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
import org.skellig.teststep.runner.context.SkelligTestContext
import org.slf4j.LoggerFactory
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.full.createInstance

open class SkelligRunner(clazz: Class<*>) : ParentRunner<FeatureRunner>(clazz) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SkelligRunner::class.java)
        private const val REPORT_LOG_ENABLED = "report.log.enabled"
    }

    private val children = mutableListOf<FeatureRunner>()
    private var reportGenerator = SkelligReportGenerator()
    private var skelligTestContext: SkelligTestContext

    init {
        val skelligOptions = clazz.getDeclaredAnnotation(SkelligOptions::class.java) as SkelligOptions
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
                        if (features.isEmpty()) throwNoFeaturesFoundError(featureResourcePath)
                    } ?: throwNoFeaturesFoundError(featureResourcePath)
                } catch (e: Exception) {
                    throw FeatureRunnerException("Failed to read features from path: $featureResourcePath", e)
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

    /*override fun runChild(child: FeatureRunner, notifier: RunNotifier) {
//        child.run(notifier)
        val childDescription = describeChild(child)
        try {
//            notifier.fireTestSuiteStarted(childDescription)
            child.runBeforeHooks(notifier)
            child.run(notifier)
        } catch (e: Throwable) {
            notifier.fireTestFailure(Failure(childDescription, e))
        } finally {
            child.runAfterHooks(notifier)
//            notifier.fireTestSuiteFinished(childDescription)
        }
    }*/

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

    private fun throwNoFeaturesFoundError(featureResourcePath: String) {
        error("No Skellig Feature files found in '$featureResourcePath'")
    }

    private val scheduler = object : RunnerScheduler {
        override fun schedule(childStatement: Runnable?) {
            childStatement?.run()
        }

        override fun finished() {

        }
    }

    private class RunSkellig(private val runFeatures: Statement) : Statement() {
        @Throws(Throwable::class)
        override fun evaluate() {
            runFeatures.evaluate()
        }
    }
}