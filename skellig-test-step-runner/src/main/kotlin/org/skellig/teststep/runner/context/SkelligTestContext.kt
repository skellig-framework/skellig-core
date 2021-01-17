package org.skellig.teststep.runner.context

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.skellig.teststep.processing.converter.*
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.factory.*
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
import org.skellig.teststep.runner.*
import org.skellig.teststep.runner.exception.TestStepRegistryException
import org.skellig.teststep.runner.model.TestStepFileExtension
import java.io.Closeable
import java.net.URISyntaxException
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

open class SkelligTestContext : Closeable {

    private var testStepValueConverter: TestStepValueConverter? = null
    private var testStepResultConverter: TestStepResultConverter? = null
    private var testDataConverter: TestDataConverter? = null
    private var testScenarioState: TestScenarioState? = null
    private var testStepResultValidator: TestStepResultValidator? = null
    private var defaultTestStepProcessor: TestStepProcessor<TestStep>? = null

    fun initialize(classLoader: ClassLoader, testStepPaths: List<String>, configPath: String? = null): TestStepRunner {
        config = createConfig(configPath)
        val testStepReader = createTestStepReader()
        testScenarioState = createTestScenarioState()
        val valueExtractor = createTestStepValueExtractor()
        testStepValueConverter = createTestStepValueConverter(classLoader, valueExtractor, testScenarioState)
        testDataConverter = createTestDataConverter(classLoader)
        testStepResultConverter = createTestDataResultConverter()
        testStepResultValidator = createTestStepValidator(valueExtractor)
        val testStepProcessors = testStepProcessors
        val testStepProcessor = createTestStepProcessor(testStepProcessors, testScenarioState)
        val testStepsRegistry = createTestStepsRegistry(testStepPaths, classLoader, testStepReader)
        val testStepFactory = createTestStepFactory(testStepProcessors, testStepsRegistry)

        return DefaultTestStepRunner.Builder()
                .withTestStepsRegistry(testStepsRegistry)
                .withTestStepProcessor(testStepProcessor)
                .withTestStepFactory(testStepFactory)
                .build()
    }

    private fun createTestStepsRegistry(testStepPaths: List<String>, classLoader: ClassLoader, testStepReader: TestStepReader): DefaultTestStepsRegistry {
        val paths = extractTestStepPaths(testStepPaths, classLoader)
        val testStepClassPaths = extractTestStepPackages(testStepPaths)
        val testStepsRegistry = TestStepsRegistry(TestStepFileExtension.STD, testStepReader)
        testStepsRegistry.registerFoundTestStepsInPath(paths)
        val classTestStepsRegistry = ClassTestStepsRegistry(testStepClassPaths, classLoader)

        return DefaultTestStepsRegistry(listOf(testStepsRegistry, classTestStepsRegistry))
    }

    private fun extractTestStepPackages(testStepPaths: Collection<String>): Collection<String> {
        return testStepPaths
                .filter { !it.contains("/") }
                .toSet()
    }

    private fun extractTestStepPaths(testStepPaths: Collection<String>?, classLoader: ClassLoader): Collection<Path> {
        return testStepPaths
                ?.filter { !it.contains(".") }
                ?.map {
                    try {
                        val resource = classLoader.getResource(it)
                        return@map resource?.let { Paths.get(resource.toURI()) }
                    } catch (e: URISyntaxException) {
                        throw TestStepRegistryException(e.message, e)
                    }
                }
                ?.filterNotNull()
                ?.toSet() ?: emptySet()
    }

    private fun createConfig(configPath: String?): Config? {
        return configPath?.let { ConfigFactory.load(configPath) }
    }

    private fun createTestStepFactory(testStepProcessors: List<TestStepProcessorDetails>, testStepsRegistry: TestStepRegistry): TestStepFactory<TestStep> {
        val testStepFactoryBuilder = CompositeTestStepFactory.Builder()
        testStepProcessors.forEach { testStepFactoryBuilder.withTestStepFactory(it.testStepFactory) }

        return testStepFactoryBuilder
                .withKeywordsProperties(testStepKeywordsProperties)
                .withTestStepValueConverter(testStepValueConverter)
                .withTestDataConverter(testDataConverter)
                .withTestDataRegistry(testStepsRegistry)
                .build()
    }

    fun getTestScenarioState(): TestScenarioState {
        return testScenarioState ?: error("TestScenarioState must be initialized first. Did you forget to call 'initialize'?")
    }

    fun getTestStepResultValidator(): TestStepResultValidator {
        return testStepResultValidator
                ?: error("TestStepResultValidator must be initialized first. Did you forget to call 'initialize'?")
    }

    fun getTestStepResultConverter(): TestStepResultConverter {
        return testStepResultConverter
                ?: error("TestStepResultConverter must be initialized first. Did you forget to call 'initialize'?")
    }

    private fun createTestStepProcessor(additionalTestStepProcessors: List<TestStepProcessorDetails>,
                                        testScenarioState: TestScenarioState?): TestStepProcessor<TestStep> {
        val testStepProcessorBuilder = CompositeTestStepProcessor.Builder()
        additionalTestStepProcessors.forEach { testStepProcessorBuilder.withTestStepProcessor(it.testStepProcessor) }

        defaultTestStepProcessor = testStepProcessorBuilder
                .withTestScenarioState(testScenarioState)
                .withValidator(getTestStepResultValidator())
                .withTestStepResultConverter(getTestStepResultConverter())
                .build()

        return defaultTestStepProcessor!!
    }

    private fun createTestStepValidator(valueExtractor: TestStepValueExtractor): TestStepResultValidator {
        val valueComparatorBuilder = DefaultValueComparator.Builder()
        additionalValueComparators.forEach { valueComparatorBuilder.withValueComparator(it) }

        return DefaultTestStepResultValidator.Builder()
                .withValueExtractor(valueExtractor)
                .withValueComparator(valueComparatorBuilder.build())
                .build()
    }

    private fun createTestStepValueConverter(classLoader: ClassLoader, valueExtractor: TestStepValueExtractor,
                                             testScenarioState: TestScenarioState?): TestStepValueConverter {
        val valueConverterBuilder = DefaultValueConverter.Builder()
        additionalTestStepValueConverters.forEach { valueConverterBuilder.withValueConverter(it) }

        return valueConverterBuilder
                .withClassLoader(classLoader)
                .withGetPropertyFunction(propertyExtractorFunction)
                .withTestScenarioState(testScenarioState)
                .withTestStepValueExtractor(valueExtractor)
                .build()
    }

    private fun createTestDataConverter(classLoader: ClassLoader): TestDataConverter {
        val builder = DefaultTestDataConverter.Builder()
        additionalTestDataConverters.forEach { builder.withTestDataConverter(it) }

        return builder.withClassLoader(classLoader).build()
    }

    private fun createTestDataResultConverter(): TestStepResultConverter {
        val builder = DefaultTestStepResultConverter.Builder()
        additionalTestStepResultConverters.forEach { builder.withTestStepResultConverter(it) }

        return builder.build()
    }

    private fun createTestStepValueExtractor(): TestStepValueExtractor {
        val valueExtractorBuilder = DefaultValueExtractor.Builder()
        additionalTestStepValueExtractors.forEach { valueExtractorBuilder.withValueExtractor(it) }

        return valueExtractorBuilder.build()
    }

    protected fun createTestStepReader(): TestStepReader {
        return StsTestStepReader()
    }

    protected fun createTestScenarioState(): TestScenarioState {
        return DefaultTestScenarioState()
    }

    protected val additionalTestStepValueExtractors: List<TestStepValueExtractor>
        protected get() = emptyList()

    protected val additionalValueComparators: List<ValueComparator>
        protected get() = emptyList()

    protected val additionalTestStepValueConverters: List<TestStepValueConverter>
        protected get() = emptyList()

    protected open val additionalTestDataConverters: List<TestDataConverter>
        protected get() = emptyList()

    protected val additionalTestStepResultConverters: List<TestStepResultConverter>
        protected get() = emptyList()

    protected open val testStepProcessors: List<TestStepProcessorDetails>
        protected get() = emptyList()

    protected open val propertyExtractorFunction: ((String) -> String?)?
        protected get() = { key -> config?.getString(key) }

    protected open val testStepKeywordsProperties: Properties?
        protected get() = null

    protected var config: Config? = null
        protected get() = field
        private set

    protected fun createTestStepFactoryFrom(delegate: (keywordsProperties: Properties?,
                                                       testStepValueConverter: TestStepValueConverter?,
                                                       testDataConverter: TestDataConverter?) -> TestStepFactory<out TestStep>): TestStepFactory<out TestStep> {
        return delegate(testStepKeywordsProperties,
                testStepValueConverter
                        ?: error("TestStepValueConverter must be initialized first. Did you forget to call 'initialize'?"),
                testDataConverter ?: error("TestDataConverter must be initialized first. Did you forget to call 'initialize'?"))
    }

    override fun close() {
        defaultTestStepProcessor!!.close()
    }

    protected class TestStepProcessorDetails(val testStepProcessor: TestStepProcessor<*>, val testStepFactory: TestStepFactory<out TestStep>)

}