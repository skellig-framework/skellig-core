package org.skellig.runner

import org.apache.log4j.BasicConfigurator
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import org.junit.runners.ParentRunner
import org.junit.runners.model.RunnerScheduler
import org.junit.runners.model.Statement
import org.skellig.feature.Feature
import org.skellig.feature.hook.DefaultSkelligHookRunner
import org.skellig.feature.hook.DefaultSkelligTestHooksRegistry
import org.skellig.feature.metadata.TagsFilter
import org.skellig.feature.parser.DefaultFeatureParser
import org.skellig.runner.annotation.SkelligOptions
import org.skellig.runner.exception.FeatureRunnerException
import org.skellig.runner.junit.report.CustomAppender
import org.skellig.runner.junit.report.DefaultTestStepLogger
import org.skellig.runner.junit.report.SkelligReportGenerator
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.runner.context.SkelligTestContext
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.full.createInstance



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
    private var reportGenerator = SkelligReportGenerator()
    private var skelligTestContext: SkelligTestContext

    init {
        log.info("Start to initialize Skellig Runner")
        val skelligOptions = clazz.getDeclaredAnnotation(SkelligOptions::class.java) as SkelligOptions

        log.info("Reading the config from file '${skelligOptions.config}'")
        val config = getConfig(skelligOptions.config)
        val classInstanceRegistry = ConcurrentHashMap<Class<*>, Any>()
        skelligTestContext = skelligOptions.context.createInstance()
        val testStepRunner = skelligTestContext.initialize(
            clazz.classLoader, skelligOptions.testSteps.toList(), config, classInstanceRegistry
        )
        val testScenarioState = skelligTestContext.getTestScenarioState()

        val testStepLogger = DefaultTestStepLogger()
        skelligTestContext.config?.let {
            if (it.hasPath(REPORT_LOG_ENABLED) && it.getBoolean(REPORT_LOG_ENABLED))
                BasicConfigurator.configure(CustomAppender(testStepLogger))
        }

        val featureParser = DefaultFeatureParser()
        val hookRunner = DefaultSkelligHookRunner(DefaultSkelligTestHooksRegistry(skelligOptions.testSteps.toList(), classInstanceRegistry))

        val includeTags = System.getProperty("skellig.includeTags")?.split(",")?.map { it.trim() }?.toSet() ?: skelligOptions.includeTags.toSet()
        val excludeTags = System.getProperty("skellig.excludeTags")?.split(",")?.map { it.trim() }?.toSet() ?: skelligOptions.excludeTags.toSet()
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
                                FeatureRunner.create(
                                    feature,
                                    testStepRunner,
                                    testScenarioState,
                                    testStepLogger,
                                    hookRunner,
                                    tagsFilter,
                                )
                            }
                            .toCollection(children)
                        if (features.isEmpty()) throwNoFeaturesFoundError(featureResourcePath, includeTags, excludeTags)
                    } ?: throwNoFeaturesFoundError(featureResourcePath, includeTags, excludeTags)
                } catch (e: Exception) {
                    throw FeatureRunnerException("Failed to read features from path: $featureResourcePath", e)
                }
            }
        log.info("Skellig Runner initialized successfully with test steps from '${skelligOptions.testSteps.joinToString(",")}' " +
                "and features from '${skelligOptions.features.joinToString(",")}'")
    }

    override fun runChild(child: FeatureRunner, notifier: RunNotifier) {
        child.run(notifier)
    }

    override fun run(notifier: RunNotifier) {
        try {
            super.run(notifier)
        } finally {
            log.info("Start to generate report out of the collected run results for all features")
            reportGenerator.generate(getChildren().map { it.getFeatureReportDetails() }.toList())
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
                try {
                    getChildren().forEach { feature -> scheduler.schedule { runChild(feature, notifier) } }
                } catch (ex: Exception) {
                    scheduler.finished()
                }
            }
        }
    }

    protected fun getConfig(config: String): String {
        val key = config.substringAfter("\${").substringBefore("}")
        val property = System.getProperty(key, "")
        return config.replace("\${$key}", property)
    }

    private fun throwNoFeaturesFoundError(featureResourcePath: String, includeTags: Set<String>, excludeTags: Set<String>) {
        error("No features were found in '${featureResourcePath}' with the included tags '${includeTags}' and excluded tags '${excludeTags}')")
    }

    private val scheduler = object : RunnerScheduler {
        override fun schedule(childStatement: Runnable?) {
            childStatement?.run()
        }

        override fun finished() {

        }
    }

}