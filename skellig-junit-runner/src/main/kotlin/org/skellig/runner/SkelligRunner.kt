package org.skellig.runner

import com.typesafe.config.ConfigFactory
import org.apache.log4j.BasicConfigurator
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import org.junit.runners.ParentRunner
import org.junit.runners.model.Statement
import org.skellig.feature.Feature
import org.skellig.feature.event.*
import org.skellig.feature.hook.DefaultSkelligHookRunner
import org.skellig.feature.hook.DefaultSkelligTestHooksRegistry
import org.skellig.feature.hook.SkelligHookRunner
import org.skellig.feature.metadata.TagsFilter
import org.skellig.feature.parser.DefaultFeatureParser
import org.skellig.plugin.report.CustomAppender
import org.skellig.plugin.report.DefaultTestStepLogger
import org.skellig.plugin.report.TestStepLogger
import org.skellig.runner.annotation.SkelligOptions
import org.skellig.runner.exception.FeatureRunnerException
import org.skellig.runner.exception.ParallelConfigException
import org.skellig.runner.parallel.ParallelConfig
import org.skellig.runner.parallel.ParallelGroupScheduler
import org.skellig.runner.parallel.ParallelMode
import org.skellig.teststep.processing.state.TestScenarioScope
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.runner.TestStepRunner
import org.skellig.teststep.runner.context.SkelligTestContext
import java.nio.file.Paths
import kotlin.reflect.full.createInstance
import kotlin.reflect.jvm.jvmErasure


/**
 * This is the main runner for executing [Skellig features][FeatureRunner] which is set from a test runner of your project.
 * It initializes [SkelligTestContext], scans for test steps and features and make a list of [FeatureRunner]s to run.
 *
 * If there are no features found, then throws [FeatureRunnerException].
 *
 * At the end of the run of all features, it generates a Skellig Report and then closes [SkelligTestContext] and releases all resources.
 *
 * @property clazz The class that is being tested. Used to extract @[SkelligOptions] and classloader.
 * @constructor Creates an instance of SkelligRunner.
 */
open class SkelligRunner(clazz: Class<*>) : ParentRunner<FeatureRunner>(clazz) {

    companion object {
        private const val REPORT_LOG_ENABLED = "report.log.enabled"
    }

    private val log = logger<SkelligRunner>()
    private val children = mutableListOf<FeatureRunner>()
    private var skelligTestContext: SkelligTestContext
    private val parallelConfig: ParallelConfig
    private val eventDispatcher = DefaultSkelligTestEventDispatcher()

    init {
        log.info("Start to initialize Skellig Runner")
        val skelligOptions = clazz.getDeclaredAnnotation(SkelligOptions::class.java) as SkelligOptions

        initPlugins(clazz)

        log.info("Reading the config from file '${skelligOptions.config}'")
        // without a configuration file, the context has no configuration
        val config = getConfig(skelligOptions.config).takeIf { it.isNotBlank() }
        skelligTestContext = skelligOptions.context.createInstance()
        val testStepRunner = skelligTestContext.initialize(clazz.classLoader, skelligOptions.testSteps.toList(), config)
        parallelConfig = try {
            readParallelConfig(clazz.classLoader)
        } catch (e: Exception) {
            // the context has started its processors already
            skelligTestContext.close()
            throw e
        }
        skelligTestContext.parallelRun = parallelConfig.isParallel
        // without a parallel run, all scenarios share the only state and the instances of the user classes of the context
        // through its default scope
        val defaultScope = skelligTestContext.getDefaultScope()

        // without a parallel run, there is only the default scope, so it also collects the lines which were logged by
        // threads that were not given a scope, as all of them belong to the scenario that runs.
        // With parallel scenarios, such a line can not be attributed to any of them, so it is dropped
        val testStepLogger = DefaultTestStepLogger(defaultScope.id.takeUnless { parallelConfig.isParallel })
        skelligTestContext.config?.let {
            if (it.hasPath(REPORT_LOG_ENABLED) && it.getBoolean(REPORT_LOG_ENABLED))
                BasicConfigurator.configure(CustomAppender(testStepLogger))
        }

        val featureParser = DefaultFeatureParser()
        val hookRunner = DefaultSkelligHookRunner(DefaultSkelligTestHooksRegistry(skelligOptions.testSteps.toList()))

        val includeTags = readTags(skelligOptions.includeTags.toSet(), "skellig.includeTags")
        val excludeTags = readTags(skelligOptions.excludeTags.toSet(), "skellig.excludeTags")
        val tagsFilter = TagsFilter(includeTags, excludeTags)

        skelligOptions.features
            .forEach { featureResourcePath: String ->
                try {
                    val featuresResource = clazz.classLoader.getResource(featureResourcePath)
                    featuresResource?.let {
                        val pathToFeatures = Paths.get(featuresResource.toURI())
                        val features = featureParser.parse(pathToFeatures.toString())
                            .filter {
                                tagsFilter.checkTagsAreIncluded(it.tags) ||
                                        it.scenarios?.any { testScenario -> tagsFilter.checkTagsAreIncluded(testScenario.tags) } == true
                            }
                            .map { feature: Feature ->
                                createFeatureRunner(feature, testStepRunner, testStepLogger, hookRunner, tagsFilter, defaultScope)
                            }
                            .toCollection(children)
                        if (features.isEmpty()) throwNoFeaturesFoundError(featureResourcePath, includeTags, excludeTags)
                    } ?: throwNoFeaturesFoundError(featureResourcePath, includeTags, excludeTags)
                } catch (e: Exception) {
                    throw FeatureRunnerException("Failed to read features from path: $featureResourcePath", e)
                }
            }
        log.info(
            "Skellig Runner initialized successfully with test steps from '${skelligOptions.testSteps.joinToString(",")}' " +
                    "and features from '${skelligOptions.features.joinToString(",")}'"
        )
    }

    private fun createFeatureRunner(
        feature: Feature,
        testStepRunner: TestStepRunner,
        testStepLogger: TestStepLogger,
        hookRunner: SkelligHookRunner,
        tagsFilter: TagsFilter,
        defaultScope: TestScenarioScope
    ): FeatureRunner =
        if (parallelConfig.mode == ParallelMode.FEATURE) {
            // a feature is a parallel group, so its scenarios and its hooks share the instances of the user classes,
            // while every scenario has a state of its own
            val parallelGroup = skelligTestContext.createParallelGroupContext(feature.name)
            // a scope collects its log from the start, so the lines logged by its hooks are reported too
            val createScope = { scopeId: String ->
                skelligTestContext.createScope(scopeId, parallelGroup).also { testStepLogger.clear(it.id) }
            }
            FeatureRunner.create(
                feature, testStepRunner, testStepLogger, hookRunner, eventDispatcher, tagsFilter,
                createScope("${feature.name}#${Integer.toHexString(feature.getId())}"), createScope
            )
        } else {
            // everything runs in the default scope, which wraps the only state of the context
            FeatureRunner.create(feature, testStepRunner, testStepLogger, hookRunner, eventDispatcher, tagsFilter, defaultScope)
        }

    private fun initPlugins(clazz: Class<*>) {
        clazz.getDeclaredAnnotationsByType(SkelligOptions.Plugin::class.java).forEach { skelligPluginOptions ->
            skelligPluginOptions.name.constructors
                .filter { constructor ->
                    if (constructor.parameters.size == skelligPluginOptions.args.size) {
                        constructor.parameters.filterIndexed { index, p ->
                            skelligPluginOptions.args[index]::class == p.type.jvmErasure
                        }.any()
                    } else false
                }
                .forEach { constructor ->
                    val skelligPlugin = constructor.call(*skelligPluginOptions.args)
                    skelligPlugin.init(eventDispatcher)
                }
        }
    }

    override fun runChild(child: FeatureRunner, notifier: RunNotifier) {
        child.run(notifier)
    }

    override fun run(notifier: RunNotifier) {
        try {
            super.run(notifier)
        } finally {
            log.info("Start to generate report out of the collected run results for all features")
            eventDispatcher.dispatch(TestRunFinishedEvent())
            skelligTestContext.close()
        }
    }

    override fun getChildren(): List<FeatureRunner> {
        return children
    }

    override fun describeChild(child: FeatureRunner): Description? {
        return child.description
    }

    override fun childrenInvoker(notifier: RunNotifier): Statement? {
        return object : Statement() {
            override fun evaluate() {
                val features = getChildren()
                val scheduler =
                    if (parallelConfig.isParallel) ParallelGroupScheduler.fixedPool(parallelConfig.workersFor(features.size))
                    else ParallelGroupScheduler.sequential()
                try {
                    features.forEach { feature -> scheduler.schedule { runChild(feature, notifier) } }
                } finally {
                    // waits for all the features and reports the first unexpected exception
                    scheduler.finished()
                }
            }
        }
    }

    private fun readParallelConfig(classLoader: ClassLoader): ParallelConfig {
        // without a configuration file the defaults of the runner and the system properties still apply
        val config = skelligTestContext.config ?: ConfigFactory.defaultOverrides(classLoader)
        return ParallelConfig.read(config.withFallback(ConfigFactory.defaultReference(classLoader))).also {
            if (it.mode == ParallelMode.SCENARIO || it.mode == ParallelMode.TAG) {
                throw ParallelConfigException(
                    "Parallel mode '${it.mode.configValue}' is not supported yet, the supported modes are: " +
                            "${ParallelMode.NONE.configValue}, ${ParallelMode.FEATURE.configValue}"
                )
            }
        }
    }

    protected fun getConfig(config: String): String {
        if (!config.contains("\${")) return config
        val key = config.substringAfter("\${").substringBefore("}")
        val property = System.getProperty(key, "")
        return config.replace("\${$key}", property)
    }

    private fun readTags(tagsFromOptions: Set<String>, propertyName: String): Set<String> =
        System.getProperty(propertyName)?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.toSet() ?: tagsFromOptions

    private fun throwNoFeaturesFoundError(featureResourcePath: String, includeTags: Set<String>, excludeTags: Set<String>) {
        error("No features were found in '${featureResourcePath}' with the included tags '${includeTags}' and excluded tags '${excludeTags}')")
    }

}