package org.skellig.teststep.runner.context

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValue
import io.github.classgraph.ClassGraph
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.factory.CompositeTestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.processor.CompositeTestStepProcessor
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfig
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfigDetails
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.processing.value.config.FunctionsConfig
import org.skellig.teststep.processing.value.config.FunctionsConfigDetails
import org.skellig.teststep.processing.value.function.DefaultFunctionValueExecutor
import org.skellig.teststep.processing.value.function.FunctionValueExecutor
import org.skellig.teststep.processing.value.property.DefaultPropertyExtractor
import org.skellig.teststep.reader.TestStepReader
import org.skellig.teststep.reader.sts.StsReader
import org.skellig.teststep.reader.value.expression.ValueExpressionContext
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


private const val DEFAULT_PACKAGE_TO_SCAN = "org.skellig.teststep.processor"

open class SkelligTestContext : Closeable {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(SkelligTestContext::class.java)
        private const val TEST_STEP_KEYWORD = "test.step.keyword"
    }

    private var valueExpressionContextFactory: ValueExpressionContextFactory? = null
    private var testScenarioState: TestScenarioState? = null
    private var rootTestStepProcessor: CompositeTestStepProcessor? = null
    private var rootTestStepFactory: CompositeTestStepFactory? = null
    private var testStepsRegistry: TestStepRegistry? = null

    fun initialize(classLoader: ClassLoader, testStepPaths: List<String>, configPath: String? = null): TestStepRunner {
        LOGGER.info(
            ("Initializing Skellig Context with test steps in '$testStepPaths'" +
                    configPath?.let { "and config file '$it'" })
        )

        config = createConfig(classLoader, configPath)

        testScenarioState = createTestScenarioState()
        val testStepClassPaths = extractTestStepPackages(testStepPaths)
        val testStepReader = createTestStepReader()
        val functionExecutor = createFunctionExecutor(classLoader, testScenarioState, testStepClassPaths)

        testStepsRegistry = createTestStepsRegistry(testStepPaths, classLoader, testStepReader, testStepClassPaths)

        valueExpressionContextFactory = ValueExpressionContextFactory(
            functionExecutor,
            DefaultPropertyExtractor(propertyExtractorFunction)
        )

        initTestStepProcessors()

        return DefaultTestStepRunner.Builder()
            .withTestStepsRegistry(getTestStepRegistry())
            .withTestStepProcessor(rootTestStepProcessor!!)
            .withTestStepFactory(rootTestStepFactory!!)
            .build()
    }

    private fun initTestStepProcessors() {
        rootTestStepProcessor = CompositeTestStepProcessor.Builder()
            .withTestScenarioState(testScenarioState)
            .build() as CompositeTestStepProcessor

        rootTestStepFactory = CompositeTestStepFactory.Builder()
            .withTestDataRegistry(getTestStepRegistry())
            .withValueExpressionContextFactory(valueExpressionContextFactory)
            .build()

        // initialise additional test step processors if found
        extractFromConfig(getPackageToScan().union(setOf(DEFAULT_PACKAGE_TO_SCAN)), TestStepProcessorConfig::class.java)
        { processorConfig ->
            try {
                val testStepProcessorConfig = processorConfig as TestStepProcessorConfig<out TestStep>
                val testStepProcessorConfigDetails = TestStepProcessorConfigDetails(
                    testScenarioState!!,
                    config!!,
                    testStepsRegistry!!,
                    valueExpressionContextFactory!!,
                    rootTestStepProcessor!!,
                    rootTestStepFactory!!
                )
                testStepProcessorConfig.config(testStepProcessorConfigDetails)
            } catch (ex: Exception) {
                throw TestStepRegistryException("Failed to instantiate class '${processorConfig.javaClass.name}'", ex)
            }
        }.filterNotNull()
            .forEach {
                rootTestStepProcessor!!.registerTestStepProcessor(it.testStepProcessor)
                injectTestContextIfRequired(it.testStepProcessor)
                rootTestStepFactory!!.registerTestStepFactory(it.testStepFactory)
                injectTestContextIfRequired(it.testStepFactory)
            }
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

        val classTestStepsRegistry = ClassTestStepsRegistry(testStepClassPaths)
        classTestStepsRegistry.getTestSteps().forEach { testStep ->
            testStep.values
                .mapNotNull { it?.evaluate(ValueExpressionContext.EMPTY) }
                .filterIsInstance<SkelligTestContextAware>()
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
            val config = ConfigFactory.load(classLoader, configPath)
            config?.let {
                if (it.hasPath(TEST_STEP_KEYWORD)) {
                    testStepKeywordsProperties = Properties()
                    it.getObject(TEST_STEP_KEYWORD)
                        .forEach { key: String, value: ConfigValue ->
                            testStepKeywordsProperties!!.setProperty("$TEST_STEP_KEYWORD.$key", value.toString())
                        }
                }
            }
            config
        }
    }

    fun getTestScenarioState(): TestScenarioState =
        testScenarioState ?: error("TestScenarioState must be initialized first. Did you forget to call 'initialize'?")

    fun getValueExpressionContextFactory(): ValueExpressionContextFactory =
        valueExpressionContextFactory
            ?: error("ValueExpressionContextFactory must be initialized first. Did you forget to call 'initialize'?")

    fun getTestStepRegistry(): TestStepRegistry =
        testStepsRegistry
            ?: error("TestStepRegistry must be initialized first. Did you forget to call 'initialize'?")

    private fun createFunctionExecutor(
        classLoader: ClassLoader,
        testScenarioState: TestScenarioState?,
        testStepClassPaths: Collection<String>
    ): FunctionValueExecutor {
        val functionExecutorBuilder = DefaultFunctionValueExecutor.Builder()

        extractFromConfig(getPackageToScan(), FunctionsConfig::class.java) { functionsConfig ->
            functionsConfig.configFunctionExecutors(FunctionsConfigDetails(testScenarioState!!, config!!))
        }.flatten()
            .forEach {
                functionExecutorBuilder.withFunctionValueExecutor(it)
                injectTestContextIfRequired(it)
            }

        return functionExecutorBuilder
            .withClassLoader(classLoader)
            .withClassPaths(testStepClassPaths)
            .withTestScenarioState(testScenarioState)
            .build()
    }

    protected open fun createTestStepReader(): TestStepReader {
        return StsReader()
    }

    protected open fun createTestScenarioState(): TestScenarioState {
        return DefaultTestScenarioState()
    }

    protected open val propertyExtractorFunction: ((String) -> Any?)?
        get() = { key -> if (config?.hasPath(key) == true) config?.getAnyRef(key) else null }

    open var testStepKeywordsProperties: Properties? = null
        get() = null
        protected set

    var config: Config? = null
        get() = field
        private set

    private fun injectTestContextIfRequired(it: Any) {
        if (it is SkelligTestContextAware) {
            it.setSkelligTestContext(this)
        }
    }

    private fun <T, C> extractFromConfig(
        packagesToScan: Set<String>,
        configClass: Class<C>,
        converter: (config: C) -> T
    ): Collection<T> {
        val functionExecutors = ClassGraph().acceptPackages(*packagesToScan.toTypedArray())
            .enableClassInfo()
            .scan()
            .use {
                it.allClasses
                    .filter { c -> c.implementsInterface(configClass) }
                    .map { c ->
                        val functionConfigClass = c.loadClass(configClass)
                        val functionsConfig = functionConfigClass.getDeclaredConstructor().newInstance()
                        converter(functionsConfig)
                    }
                    .toList()
            }
        return functionExecutors
    }

    override fun close() {
        rootTestStepProcessor?.let {
            LOGGER.info("Shutting down the Skellig Context")

            it.close()

            LOGGER.info("Skellig Context has been shut down")
        }
    }

    private fun getPackageToScan() = config?.getString("packageToScan")?.split(',')?.toSet() ?: emptySet()

}