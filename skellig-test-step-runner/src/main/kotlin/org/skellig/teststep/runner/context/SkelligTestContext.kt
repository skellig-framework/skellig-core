package org.skellig.teststep.runner.context

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.skellig.teststep.processing.converter.DefaultTestStepResultConverter
import org.skellig.teststep.processing.converter.DefaultValueConverter
import org.skellig.teststep.processing.converter.TestStepResultConverter
import org.skellig.teststep.processing.converter.TestStepValueConverter
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.factory.CompositeTestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.processor.CompositeTestStepProcessor
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.DefaultTestStepResultValidator
import org.skellig.teststep.processing.validation.TestStepResultValidator
import org.skellig.teststep.processing.validation.comparator.DefaultValueComparator
import org.skellig.teststep.processing.validation.comparator.ValueComparator
import org.skellig.teststep.processing.valueextractor.DefaultValueExtractor
import org.skellig.teststep.processing.valueextractor.TestStepValueExtractor
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
        private val LOGGER: Logger = LoggerFactory.getLogger(SkelligTestContext.javaClass)
    }

    private var testStepValueConverter: TestStepValueConverter? = null
    private var testStepResultConverter: TestStepResultConverter? = null
    private var testScenarioState: TestScenarioState? = null
    private var testStepResultValidator: TestStepResultValidator? = null
    private var rootTestStepProcessor: CompositeTestStepProcessor? = null
    private var rootTestStepFactory: CompositeTestStepFactory? = null
    private var testStepsRegistry: TestStepRegistry? = null

    fun initialize(classLoader: ClassLoader, testStepPaths: List<String>, configPath: String? = null): TestStepRunner {
        LOGGER.info(("Initializing Skellig Context with test steps in '$testStepPaths'" +
                configPath?.let { "and config file '$it'" })
        )

        config = createConfig(classLoader, configPath)
        val testStepReader = createTestStepReader()
        testScenarioState = createTestScenarioState()
        val valueExtractor = createTestStepValueExtractor()
        testStepValueConverter = createTestStepValueConverter(classLoader, valueExtractor, testScenarioState)
        testStepResultConverter = createTestDataResultConverter()
        testStepResultValidator = createTestStepValidator(valueExtractor)
        testStepsRegistry = createTestStepsRegistry(testStepPaths, classLoader, testStepReader)

        rootTestStepProcessor = CompositeTestStepProcessor.Builder()
            .withTestScenarioState(testScenarioState)
            .withValidator(getTestStepResultValidator())
            .withTestStepResultConverter(getTestStepResultConverter())
            .build() as CompositeTestStepProcessor

        rootTestStepFactory = CompositeTestStepFactory.Builder()
            .withKeywordsProperties(testStepKeywordsProperties)
            .withTestStepValueConverter(testStepValueConverter)
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
        testStepReader: TestStepReader
    ): CachedTestStepsRegistry {
        val paths = extractTestStepPaths(testStepPaths, classLoader)
        val testStepClassPaths = extractTestStepPackages(testStepPaths)
        val testStepsRegistry = TestStepsRegistry(TestStepFileExtension.STD, testStepReader)
        testStepsRegistry.registerFoundTestStepsInPath(paths)
        val classTestStepsRegistry = ClassTestStepsRegistry(testStepClassPaths, classLoader)

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

    private fun createConfig(classLoader: ClassLoader, configPath: String?): Config? {
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

    fun getTestStepResultConverter(): TestStepResultConverter =
        testStepResultConverter
            ?: error("TestStepResultConverter must be initialized first. Did you forget to call 'initialize'?")

    fun getTestStepRegistry(): TestStepRegistry =
        testStepsRegistry
            ?: error("TestStepRegistry must be initialized first. Did you forget to call 'initialize'?")


    private fun createTestStepProcessor(additionalTestStepProcessors: List<TestStepProcessorDetails>): TestStepProcessor<TestStep> {
        additionalTestStepProcessors.forEach { rootTestStepProcessor!!.registerTestStepProcessor(it.testStepProcessor) }

        return rootTestStepProcessor!!
    }

    private fun createTestStepValidator(valueExtractor: TestStepValueExtractor): TestStepResultValidator {
        val valueComparatorBuilder = DefaultValueComparator.Builder()
        additionalValueComparators.forEach { valueComparatorBuilder.withValueComparator(it) }

        return DefaultTestStepResultValidator.Builder()
            .withValueExtractor(valueExtractor)
            .withValueComparator(valueComparatorBuilder.build())
            .build()
    }

    private fun createTestStepValueConverter(
        classLoader: ClassLoader,
        valueExtractor: TestStepValueExtractor,
        testScenarioState: TestScenarioState?
    ): TestStepValueConverter {
        val valueConverterBuilder = DefaultValueConverter.Builder()
        additionalTestStepValueConverters.forEach { valueConverterBuilder.withValueConverter(it) }

        return valueConverterBuilder
            .withClassLoader(classLoader)
            .withGetPropertyFunction(propertyExtractorFunction)
            .withTestScenarioState(testScenarioState)
            .withTestStepValueExtractor(valueExtractor)
            .build()
    }

    private fun createTestDataResultConverter(): TestStepResultConverter {
        val builder = DefaultTestStepResultConverter.Builder()
        additionalTestStepResultConverters.forEach { builder.withTestStepResultConverter(it) }

        return builder.build()
    }

    private fun createTestStepValueExtractor(): TestStepValueExtractor {
        val valueExtractorBuilder = DefaultValueExtractor.Builder()
        additionalTestStepValueExtractors.forEach { valueExtractorBuilder.valueExtractor(it) }

        return valueExtractorBuilder.build()
    }

    protected open fun createTestStepReader(): TestStepReader {
        return StsTestStepReader()
    }

    protected open fun createTestScenarioState(): TestScenarioState {
        return DefaultTestScenarioState()
    }

    protected open val additionalTestStepValueExtractors: List<TestStepValueExtractor>
        get() = emptyList()

    protected open val additionalValueComparators: List<ValueComparator>
        get() = emptyList()

    protected open val additionalTestStepValueConverters: List<TestStepValueConverter>
        get() = emptyList()

    protected open val additionalTestStepResultConverters: List<TestStepResultConverter>
        get() = emptyList()

    protected open val testStepProcessors: List<TestStepProcessorDetails>
        get() = emptyList()

    protected open val propertyExtractorFunction: ((String) -> String?)?
        get() = { key -> config?.getString(key) }

    open val testStepKeywordsProperties: Properties?
        get() = null

    protected var config: Config? = null
        protected get() = field
        private set

    protected fun <T : TestStep> createTestStepProcessorFrom(
        createTestStepProcessorDelegate: (
            rootTestStepProcessor: TestStepProcessor<TestStep>
        ) -> TestStepProcessor<T>,
        createTestStepFactoryDelegate: (
            rootTestStepFactory: TestStepFactory<TestStep>,
            keywordsProperties: Properties?,
            testStepValueConverter: TestStepValueConverter?
        ) -> TestStepFactory<T>
    ): TestStepProcessorDetails {
        return TestStepProcessorDetails(
            createTestStepProcessorDelegate(rootTestStepProcessor!!),
            createTestStepFactoryDelegate(
                rootTestStepFactory!!,
                testStepKeywordsProperties,
                testStepValueConverter
                    ?: error("TestStepValueConverter must be initialized first. Did you forget to call 'initialize'?")
            )
        )
    }

    protected fun <T : TestStep> createTestStepProcessorFrom(
        testStepProcessor: TestStepProcessor<T>,
        createTestStepFactoryDelegate: (
            keywordsProperties: Properties?,
            testStepValueConverter: TestStepValueConverter?
        ) -> TestStepFactory<T>
    ): TestStepProcessorDetails {
        return createTestStepProcessorFrom(
            { testStepProcessor },
            { _, keywordsProperties, testStepValueConverter ->
                createTestStepFactoryDelegate(
                    keywordsProperties,
                    testStepValueConverter
                )
            })
    }

    override fun close() {
        LOGGER.info("Shutting down the Skellig Context")

        rootTestStepProcessor!!.close()

        LOGGER.info("Skellig Context has been shut down")
    }

    protected class TestStepProcessorDetails(
        val testStepProcessor: TestStepProcessor<*>,
        val testStepFactory: TestStepFactory<out TestStep>
    )

}