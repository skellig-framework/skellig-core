package org.skellig.teststep.runner.context

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.factory.CompositeTestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepFactoryValueConverter
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.processor.CompositeTestStepProcessor
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.DefaultTestStepResultValidator
import org.skellig.teststep.processing.validation.TestStepResultValidator
import org.skellig.teststep.processing.validation.comparator.DefaultValueComparator
import org.skellig.teststep.processing.validation.comparator.ValueComparator
import org.skellig.teststep.processing.value.chunk.RawValueProcessingVisitor
import org.skellig.teststep.processing.value.extractor.DefaultValueExtractor
import org.skellig.teststep.processing.value.extractor.ValueExtractor
import org.skellig.teststep.processing.value.function.DefaultFunctionValueExecutor
import org.skellig.teststep.processing.value.function.FunctionValueExecutor
import org.skellig.teststep.processing.value.property.DefaultPropertyExtractor
import org.skellig.teststep.reader.TestStepReader
import org.skellig.teststep.reader.sts.StsTestStepReader
import org.skellig.teststep.runner.DefaultTestStepRunner
import org.skellig.teststep.runner.TestStepRunner
import org.skellig.teststep.runner.exception.TestStepRegistryException
import org.skellig.teststep.runner.model.TestStepFileExtension
import org.skellig.teststep.runner.registry.CachedTestStepsRegistry
import org.skellig.teststep.runner.registry.ClassTestStepsRegistry
import org.skellig.teststep.runner.registry.TestStepsRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.net.URI
import java.net.URISyntaxException
import java.util.*

abstract class SkelligTestContext : Closeable {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(SkelligTestContext::class.java)
    }

    private var testStepFactoryValueConverter: TestStepFactoryValueConverter? = null
    private var testScenarioState: TestScenarioState? = null
    private var testStepResultValidator: TestStepResultValidator? = null
    private var rootTestStepProcessor: CompositeTestStepProcessor? = null
    private var rootTestStepFactory: CompositeTestStepFactory? = null
    private var testStepsRegistry: TestStepRegistry? = null

    fun initialize(classLoader: ClassLoader, testStepPaths: List<String>, configPath: String? = null): TestStepRunner {
        LOGGER.info(
            ("Initializing Skellig Context with test steps in '$testStepPaths'" +
                    configPath?.let { "and config file '$it'" })
        )

        config = createConfig(classLoader, configPath)
        val testStepClassPaths = extractTestStepPackages(testStepPaths)
        val testStepReader = createTestStepReader()
        testScenarioState = createTestScenarioState()
        val valueExtractor = createTestStepValueExtractor()
        val valueComparator = createValueComparator()

        val rawValueProcessingVisitor = RawValueProcessingVisitor(
            createTestStepValueConverter(classLoader, testScenarioState, testStepClassPaths),
            valueExtractor,
            valueComparator,
            DefaultPropertyExtractor(propertyExtractorFunction)
        )

        testStepResultValidator = createTestStepValidator(rawValueProcessingVisitor)
        testStepsRegistry = createTestStepsRegistry(testStepPaths, classLoader, testStepReader, testStepClassPaths)


        testStepFactoryValueConverter =
            TestStepFactoryValueConverter.Builder()
                .withValueProcessingVisitor(rawValueProcessingVisitor)
                .build()

        rootTestStepProcessor = CompositeTestStepProcessor.Builder()
            .withTestScenarioState(testScenarioState)
            .withValidator(getTestStepResultValidator())
            .build() as CompositeTestStepProcessor

        rootTestStepFactory = CompositeTestStepFactory.Builder()
            .withKeywordsProperties(testStepKeywordsProperties)
            .withTestStepFactoryValueConverter(testStepFactoryValueConverter!!)
            .withTestDataRegistry(getTestStepRegistry())
            .build()

        val testStepProcessors = testStepProcessors
        val testStepProcessor = createTestStepProcessor(testStepProcessors)
        val testStepFactory = createTestStepFactory(testStepProcessors, getTestStepRegistry())

        return DefaultTestStepRunner.Builder()
            .withTestStepsRegistry(getTestStepRegistry())
            .withTestStepProcessor(testStepProcessor)
            .withTestStepFactory(testStepFactory)
            .build()
    }

    private fun createTestStepsRegistry(
        testStepPaths: List<String>,
        classLoader: ClassLoader,
        testStepReader: TestStepReader,
        testStepClassPaths: Collection<String>
    ): CachedTestStepsRegistry {
        val paths = extractTestStepPaths(testStepPaths, classLoader)
        val testStepsRegistry = TestStepsRegistry(TestStepFileExtension.STS, testStepReader)
        testStepsRegistry.registerFoundTestStepsInPath(paths)

        val classTestStepsRegistry = ClassTestStepsRegistry(testStepClassPaths, classLoader)
        classTestStepsRegistry.getTestSteps().forEach { testStep ->
            testStep.values.filterIsInstance<SkelligTestContextAware>()
                .forEach { it.setSkelligTestContext(this) }
        }
        return CachedTestStepsRegistry(listOf(testStepsRegistry, classTestStepsRegistry))
    }

    private fun extractTestStepPackages(testStepPaths: Collection<String>): Collection<String> {
        return testStepPaths
            .filter { !it.contains("/") }
            .toSet()
    }

    private fun extractTestStepPaths(testStepPaths: Collection<String>?, classLoader: ClassLoader): Collection<URI> {
        return testStepPaths
            ?.map {
                try {
                    val resource = classLoader.getResource(it)
                    return@map resource?.let { resource.toURI() }
                } catch (e: URISyntaxException) {
                    throw TestStepRegistryException(e.message, e)
                }
            }
            ?.filterNotNull()
            ?.toSet() ?: emptySet()
    }

    protected open fun createConfig(classLoader: ClassLoader, configPath: String?): Config? {
        return configPath?.let {
            if (classLoader.getResource(configPath) == null) {
                throw IllegalArgumentException("Path to config file $configPath does not exist")
            }
            ConfigFactory.load(classLoader, configPath)
        }
    }

    private fun createTestStepFactory(
        testStepProcessorsDetails: List<TestStepProcessorDetails>,
        testStepsRegistry: TestStepRegistry
    ): TestStepFactory<TestStep> {
        testStepProcessorsDetails.forEach { rootTestStepFactory!!.registerTestStepFactory(it.testStepFactory) }

        return rootTestStepFactory!!
    }

    fun getTestScenarioState(): TestScenarioState =
        testScenarioState ?: error("TestScenarioState must be initialized first. Did you forget to call 'initialize'?")

    fun getTestStepResultValidator(): TestStepResultValidator =
        testStepResultValidator
            ?: error("TestStepResultValidator must be initialized first. Did you forget to call 'initialize'?")

    fun getTestStepRegistry(): TestStepRegistry =
        testStepsRegistry
            ?: error("TestStepRegistry must be initialized first. Did you forget to call 'initialize'?")


    private fun createTestStepProcessor(additionalTestStepProcessors: List<TestStepProcessorDetails>): TestStepProcessor<TestStep> {
        additionalTestStepProcessors.forEach {
            rootTestStepProcessor!!.registerTestStepProcessor(it.testStepProcessor)
            injectTestContextIfRequired(it.testStepFactory)
            injectTestContextIfRequired(it.testStepProcessor)
        }

        return rootTestStepProcessor!!
    }

    private fun createValueComparator(): ValueComparator {
        val valueComparatorBuilder = DefaultValueComparator.Builder()
        additionalValueComparators.forEach {
            valueComparatorBuilder.withValueComparator(it)
            injectTestContextIfRequired(it)
        }
        return valueComparatorBuilder.build()
    }

    private fun createTestStepValidator(
        rawValueProcessingVisitor: RawValueProcessingVisitor,
    ): TestStepResultValidator {
        return DefaultTestStepResultValidator.Builder()
            .withValueProcessingVisitor(rawValueProcessingVisitor)
            .build()
    }

    private fun createTestStepValueConverter(
        classLoader: ClassLoader,
        testScenarioState: TestScenarioState?,
        testStepClassPaths: Collection<String>
    ): FunctionValueExecutor {
        val valueConverterBuilder = DefaultFunctionValueExecutor.Builder()
        additionalFunctionExecutors.forEach {
            valueConverterBuilder.withFunctionValueExecutor(it)
            injectTestContextIfRequired(it)
        }

        return valueConverterBuilder
            .withClassLoader(classLoader)
            .withClassPaths(testStepClassPaths)
            .withGetPropertyFunction(propertyExtractorFunction)
            .withTestScenarioState(testScenarioState)
            .build()
    }

    private fun createTestStepValueExtractor(): ValueExtractor {
        val valueExtractorBuilder = DefaultValueExtractor.Builder()
        additionalValueExtractors.forEach {
            injectTestContextIfRequired(it)
        }

        return valueExtractorBuilder.build()
    }

    protected open fun createTestStepReader(): TestStepReader {
        return StsTestStepReader()
    }

    protected open fun createTestScenarioState(): TestScenarioState {
        return DefaultTestScenarioState()
    }

    protected open val additionalValueExtractors: List<ValueExtractor>
        get() = emptyList()

    protected open val additionalValueComparators: List<ValueComparator>
        get() = emptyList()

    protected open val additionalFunctionExecutors: List<FunctionValueExecutor>
        get() = emptyList()

    protected open val testStepProcessors: List<TestStepProcessorDetails>
        get() = emptyList()

    protected open val propertyExtractorFunction: ((String) -> Any?)?
        get() = { key -> if (config?.hasPath(key) == true) config?.getAnyRef(key) else null }

    open val testStepKeywordsProperties: Properties?
        get() = null

    var config: Config? = null
        get() = field
        private set

    protected fun <T : TestStep> createTestStepProcessorFrom(
        createTestStepProcessorDelegate: (
            rootTestStepProcessor: TestStepProcessor<TestStep>
        ) -> TestStepProcessor<T>,
        createTestStepFactoryDelegate: (
            rootTestStepFactory: TestStepFactory<TestStep>,
            keywordsProperties: Properties?,
            testStepFactoryValueConverter: TestStepFactoryValueConverter
        ) -> TestStepFactory<T>
    ): TestStepProcessorDetails {
        return TestStepProcessorDetails(
            createTestStepProcessorDelegate(rootTestStepProcessor!!),
            createTestStepFactoryDelegate(
                rootTestStepFactory!!,
                testStepKeywordsProperties,
                testStepFactoryValueConverter
                    ?: error("TestStepFactoryValueConverter must be initialized first. Did you forget to call 'initialize'?")
            )
        )
    }

    protected fun <T : DefaultTestStep> createTestStepProcessorFrom(
        testStepProcessor: TestStepProcessor<T>,
        createTestStepFactoryDelegate: (
            testStepsRegistry: TestStepRegistry,
            keywordsProperties: Properties?,
            testStepFactoryValueConverter: TestStepFactoryValueConverter
        ) -> TestStepFactory<T>
    ): TestStepProcessorDetails {
        return createTestStepProcessorFrom(
            { testStepProcessor },
            { _, keywordsProperties, testStepFactoryValueConverter ->
                createTestStepFactoryDelegate(
                    testStepsRegistry!!,
                    keywordsProperties,
                    testStepFactoryValueConverter
                )
            })
    }

    private fun injectTestContextIfRequired(it: Any) {
        if (it is SkelligTestContextAware) {
            it.setSkelligTestContext(this)
        }
    }

    override fun close() {
        rootTestStepProcessor?.let {
            LOGGER.info("Shutting down the Skellig Context")

            it.close()

            LOGGER.info("Skellig Context has been shut down")
        }
    }

    protected class TestStepProcessorDetails(
        val testStepProcessor: TestStepProcessor<*>,
        val testStepFactory: TestStepFactory<out TestStep>
    )

}