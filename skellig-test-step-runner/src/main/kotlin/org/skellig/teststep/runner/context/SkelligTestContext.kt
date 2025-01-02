package org.skellig.teststep.runner.context

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.github.classgraph.ClassGraph
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.factory.CompositeTestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.processor.CompositeTestStepProcessor
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfig
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfigDetails
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.util.debug
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.processing.value.config.FunctionsConfig
import org.skellig.teststep.processing.value.config.FunctionsConfigDetails
import org.skellig.teststep.processing.value.function.DefaultFunctionValueExecutor
import org.skellig.teststep.processing.value.function.FunctionValueExecutor
import org.skellig.teststep.processing.value.property.DefaultPropertyExtractor
import org.skellig.teststep.reader.TestStepReader
import org.skellig.teststep.reader.sts.StsReader
import org.skellig.teststep.reader.value.expression.ValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpressionContext
import org.skellig.teststep.runner.DefaultTestStepRunner
import org.skellig.teststep.runner.TestStepRunner
import org.skellig.teststep.runner.exception.TestStepRegistryException
import org.skellig.teststep.runner.model.TestStepFileExtension
import org.skellig.teststep.runner.registry.CachedTestStepsRegistry
import org.skellig.teststep.runner.registry.ClassTestStepsRegistry
import org.skellig.teststep.runner.registry.TestStepsRegistry
import java.io.Closeable
import java.net.URI
import java.net.URISyntaxException
import java.util.concurrent.ConcurrentHashMap

private const val DEFAULT_PACKAGE_TO_SCAN = "org.skellig.teststep.processor"

/**
 * The `SkelligTestContext` class is a main entry-point of Skellig Framework which represents a context for keeping
 * necessary interfaces to interact with Skellig Test Steps, such as:
 * - [TestStepRunner]
 * - [TestStepRegistry]
 * - [TestScenarioState]
 * - [ValueExpressionContextFactory]
 * - [Config]
 *
 * The main interfaces which Skellig Context provides are [TestStepRunner] and [TestScenarioState] and the rest are available
 * for convenience of working with other tasks, such as: reading properties of Skellig Config, taking out a raw Test Step
 * from registry or evaluating [ValueExpression] in the code using [ValueExpressionContextFactory].
 *
 * When [SkelligTestContext.initialize] is called, it does the following:
 * 1) Scans the Skellig [Config] file, paths to Test Step files
 * 2) Scans for [FunctionsConfig] implementations to initialize built-in and user-created functions for Skellig Test Steps.
 * 3) Scans for [TestStepProcessorConfig] implementations to initialize built-in and user-created test processors for Skellig Test Steps.
 * After initialization is completed, it returns the [TestStepRunner] - the main interface to run test steps.
 *
 * SkelligTestContext implements [Closeable] interface and by calling method [Closeable.close], it will close all active
 * Test Step processors and release all opened resources. By calling this method you won't be able to run test steps anymore
 * as there will be no active Test Step processors to process them. You must call [SkelligTestContext.initialize] method
 * again or create a new instance of [TestStepRunner].
 *
 * @constructor Creates a new instance of SkelligTestContext.
 */
open class SkelligTestContext : Closeable {

    private val log = logger<SkelligTestContext>()
    private var valueExpressionContextFactory: ValueExpressionContextFactory? = null
    private var testScenarioState: TestScenarioState? = null
    private var rootTestStepProcessor: CompositeTestStepProcessor? = null
    private var rootTestStepFactory: CompositeTestStepFactory? = null

    fun initialize(
        classLoader: ClassLoader, testStepPaths: List<String>, configPath: String? = null,
        classInstanceRegistry: MutableMap<Class<*>, Any>? = null
    ): TestStepRunner {
        log.info(
            "Initializing Skellig Context with test steps in '$testStepPaths'" +
                    configPath?.let { "and config file '$it'" }
        )
        val newClassInstanceRegistry = classInstanceRegistry ?: ConcurrentHashMap<Class<*>, Any>()

        config = getConfig(classLoader, configPath)

        testScenarioState = createTestScenarioState()
        val testStepClassPaths = extractTestStepPackages(testStepPaths)
        val testStepReader = createTestStepReader()
        val functionExecutor = createFunctionExecutor(classLoader, testScenarioState, testStepClassPaths, newClassInstanceRegistry)

        testStepsRegistry = createTestStepsRegistry(testStepPaths, classLoader, testStepReader, testStepClassPaths, newClassInstanceRegistry)

        valueExpressionContextFactory = ValueExpressionContextFactory(
            functionExecutor,
            DefaultPropertyExtractor(propertyExtractorFunction)
        )

        initTestStepProcessors(valueExpressionContextFactory!!, testStepsRegistry!!)

        return DefaultTestStepRunner.Builder()
            .withTestStepsRegistry(testStepsRegistry!!)
            .withTestStepProcessor(rootTestStepProcessor!!)
            .withTestStepFactory(rootTestStepFactory!!)
            .build()
    }

    private fun initTestStepProcessors(
        valueExpressionContextFactory: ValueExpressionContextFactory,
        testStepRegistry: TestStepRegistry,
    ) {
        rootTestStepFactory = CompositeTestStepFactory.Builder()
            .withTestDataRegistry(testStepRegistry)
            .withValueExpressionContextFactory(valueExpressionContextFactory)
            .build()

        rootTestStepProcessor = CompositeTestStepProcessor.Builder()
            .withValueConvertDelegate { v, p ->
                v?.evaluate(valueExpressionContextFactory.create(p))
            }
            .withProcessTestStepDelegate { name, parameters ->
                val rawTestStepToRun = testStepRegistry.getByName(name)
                    ?: error("Test step '$name' is not found in any of test data files or classes")
                val testStep = rootTestStepFactory!!.create(name, rawTestStepToRun, parameters)
                rootTestStepProcessor!!.process(testStep)
            }
            .withTestScenarioState(testScenarioState)
            .build() as CompositeTestStepProcessor

        // initialise additional test step processors if found
        extractFromConfig(getPackageToScan().union(setOf(DEFAULT_PACKAGE_TO_SCAN)), TestStepProcessorConfig::class.java)
        { processorConfig ->
            try {
                val testStepProcessorConfig = processorConfig as TestStepProcessorConfig<out TestStep>
                log.debug { "Found a config for Test Step Processing: '${processorConfig.javaClass.name}'" }
                val testStepProcessorConfigDetails = TestStepProcessorConfigDetails(
                    testScenarioState!!,
                    config!!,
                    testStepsRegistry!!,
                    valueExpressionContextFactory,
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
        testStepClassPaths: Collection<String>,
        classInstanceRegistry: MutableMap<Class<*>, Any>
    ): CachedTestStepsRegistry {
        val classTestStepsRegistry = ClassTestStepsRegistry(testStepClassPaths, classInstanceRegistry)
        classTestStepsRegistry.getTestSteps().forEach { testStep ->
            testStep.values
                .mapNotNull { it?.evaluate(ValueExpressionContext.EMPTY) }
                .filterIsInstance<SkelligTestContextAware>()
                .forEach { it.setSkelligTestContext(this) }
        }
        return CachedTestStepsRegistry(listOf(getTestStepRegistry(testStepPaths, classLoader, testStepReader), classTestStepsRegistry))
    }


    private fun extractTestStepPackages(testStepPaths: Collection<String>): Collection<String> {
        return testStepPaths
            .filter { !it.contains("/") }
            .toSet()
    }

    /**
     * Returns the configured instance of [TestScenarioState]. This instance is used across all test scenarios until the
     * [SkelligTestContext] is [closed][SkelligTestContext.close].
     * If the testScenarioState is null, it throws an error stating that the TestScenarioState must be initialized first.
     * Make sure to call the 'initialize' method before calling this method.
     *
     * @return the current state of the test scenario
     * @throws IllegalStateException if the TestScenarioState is not initialized
     */
    fun getTestScenarioState(): TestScenarioState =
        testScenarioState ?: error("TestScenarioState must be initialized first. Did you forget to call 'initialize'?")

    /**
     * Returns the configured instance of [ValueExpressionContextFactory].
     *
     * @throws IllegalStateException if [ValueExpressionContextFactory] is not initialized.
     * Make sure to call the 'initialize' method before calling this method.
     */
    fun getValueExpressionContextFactory(): ValueExpressionContextFactory =
        valueExpressionContextFactory
            ?: error("ValueExpressionContextFactory must be initialized first. Did you forget to call 'initialize'?")

    /**
     * Returns the configured instance of [TestStepRegistry].
     *
     * @return the instance of [TestStepRegistry]
     * @throws IllegalStateException if [TestStepRegistry] is not initialized.
     *         Make sure to call the 'initialize' method before calling this method.
     */
    fun getTestStepRegistry(): TestStepRegistry =
        testStepsRegistry
            ?: error("TestStepRegistry must be initialized first. Did you forget to call 'initialize'?")

    private fun createFunctionExecutor(
        classLoader: ClassLoader,
        testScenarioState: TestScenarioState?,
        testStepClassPaths: Collection<String>,
        classInstanceRegistry: MutableMap<Class<*>, Any>
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
            .withClassInstanceRegistry(classInstanceRegistry)
            .withClassLoader(classLoader)
            .withClassPaths(testStepClassPaths)
            .withTestScenarioState(testScenarioState)
            .build()
    }

    /**
     * Creates a new instance of [TestStepReader]. Unless overridden, by default it's [StsReader].
     */
    protected open fun createTestStepReader(): TestStepReader = StsReader()

    /**
     * Creates a new instance of [TestScenarioState]. Unless overridden, by default it's [DefaultTestScenarioState].
     */
    protected open fun createTestScenarioState(): TestScenarioState {
        return DefaultTestScenarioState()
    }

    /**
     * Retrieves property values from the Skellig [Config] based on the provided key.
     *
     * This function takes a key as a parameter and returns the associated value from the [Config]. If the key is not found, it returns null.
     *
     * @param key The key used to retrieve the property value from the config.
     * @return The property value associated with the provided key, or null if the key is not found.
     */
    protected open val propertyExtractorFunction: ((String) -> Any?)?
        get() = { key -> if (config?.hasPath(key) == true) config?.getAnyRef(key) else null }

    /**
     * Returns Skellig [Config] with all configurations and properties.
     */
    var config: Config? = null
        get() = field
        private set

    private fun injectTestContextIfRequired(it: Any) {
        if (it is SkelligTestContextAware) {
            log.debug { "Assign the Skellig Context into ${it.javaClass}" }
            it.setSkelligTestContext(this)
        }
    }

    private fun <T, C> extractFromConfig(
        packagesToScan: Set<String>,
        configClass: Class<C>,
        converter: (config: C) -> T
    ): Collection<T> {
        return ClassGraph().acceptPackages(*packagesToScan.toTypedArray())
            .enableClassInfo()
            .scan()
            .use {
                it.allClasses
                    .filter { c -> c.implementsInterface(configClass) }
                    .map { c ->
                        val loadedConfigClass = c.loadClass(configClass)
                        converter(loadedConfigClass.getDeclaredConstructor().newInstance())
                    }
                    .toList()
            }
    }

    /**
     * Closes the Skellig Context by shutting down all registered test step processors.
     */
    override fun close() {
        rootTestStepProcessor?.let {
            log.info("Shutting down the Skellig Context")
            it.close()
            log.info("Skellig Context has been shut down")
        }
    }

    private fun getPackageToScan(): Set<String> {
        var packagesToScan = emptySet<String>()
        if (config?.hasPath("packageToScan") == true) {
            packagesToScan = config?.getString("packageToScan")?.split(',')?.toSet() ?: emptySet()
        } else {
            log.warn(
                "No definition found for the property 'packageToScan' in Skellig Config file." +
                        "If you defined custom Test Step Processors or Functions then you should define this property" +
                        " by setting packages, comma-separated, where the respective configs for Test Step Processors and Functions are located."
            )
        }
        return packagesToScan
    }


    /**
     * Static resources for each context
     */
    companion object {
        private var config: Config? = null
        private var testStepsRegistry: TestStepRegistry? = null

        @Synchronized
        private fun getConfig(classLoader: ClassLoader, configPath: String?): Config {
            if (config == null) {
                config = configPath?.let {
                    if (classLoader.getResource(configPath) == null) {
                        throw IllegalArgumentException("Path to config file $configPath does not exist")
                    }
                    ConfigFactory.load(classLoader, configPath)
                }
            }
            return config!!
        }

        @Synchronized
        private fun getTestStepRegistry(testStepPaths: List<String>, classLoader: ClassLoader, testStepReader: TestStepReader): TestStepRegistry {
            if (testStepsRegistry == null) {
                testStepsRegistry = TestStepsRegistry(TestStepFileExtension.STS, testStepReader)
                (testStepsRegistry as TestStepsRegistry).registerFoundTestStepsInPath(extractTestStepPaths(testStepPaths, classLoader))
            }
            return testStepsRegistry!!
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
    }
}