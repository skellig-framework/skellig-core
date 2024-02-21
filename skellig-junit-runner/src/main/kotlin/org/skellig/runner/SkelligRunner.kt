package org.skellig.runner

import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import org.junit.runners.ParentRunner
import org.junit.runners.model.Statement
import org.skellig.feature.Feature
import org.skellig.feature.metadata.TagsFilter
import org.skellig.feature.parser.DefaultFeatureParser
import org.skellig.runner.annotation.SkelligOptions
import org.skellig.runner.exception.FeatureRunnerException
import org.skellig.runner.junit.report.SkelligReportGenerator
import org.skellig.teststep.runner.context.SkelligTestContext
import org.slf4j.LoggerFactory
import java.nio.file.Paths
import kotlin.reflect.full.createInstance

open class SkelligRunner(clazz: Class<*>) : ParentRunner<FeatureRunner>(clazz) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SkelligRunner::class.java)
    }

    private val children = mutableListOf<FeatureRunner>()
    private var reportGenerator = SkelligReportGenerator()
    private var skelligTestContext: SkelligTestContext? = null

    init {
        val skelligOptions = clazz.getDeclaredAnnotation(SkelligOptions::class.java) as SkelligOptions
        val config = getConfig(skelligOptions.config)
        skelligTestContext = skelligOptions.context.createInstance()
        val testStepRunner = skelligTestContext!!.initialize(clazz.classLoader, skelligOptions.testSteps.toList(), config)
        val testScenarioState = skelligTestContext!!.getTestScenarioState()
        val featureParser = DefaultFeatureParser()

        val includeTags = System.getProperty("skellig.includeTags")?.split(",")?.map { it.trim() }?.toSet() ?: skelligOptions.includeTags.toSet()
        val excludeTags = System.getProperty("skellig.excludeTags")?.split(",")?.map { it.trim() }?.toSet() ?: skelligOptions.excludeTags.toSet()
        val tagsFilter = TagsFilter(includeTags, excludeTags)

        skelligOptions.features
            .forEach { featureResourcePath: String ->
                try {
                    val featuresResource = clazz.classLoader.getResource(featureResourcePath)
                    featuresResource?.let {
                        val pathToFeatures = Paths.get(featuresResource.toURI())
                        featureParser.parse(pathToFeatures.toString())
                            ?.filter {
                                tagsFilter.checkTagsAreIncluded(it.tags) ||
                                        it.scenarios?.any { testScenario -> tagsFilter.checkTagsAreIncluded(testScenario.tags)  } == true
                            }
                            ?.map { feature: Feature ->
                                FeatureRunner.create(
                                    feature,
                                    testStepRunner,
                                    testScenarioState,
                                    skelligTestContext?.config,
                                    tagsFilter
                                )
                            }
                            ?.toCollection(children)
                            ?: error("Failed to parse features from $featureResourcePath")
                    }
                } catch (e: Exception) {
                    throw FeatureRunnerException("Failed to read features from path: $featureResourcePath", e)
                }
            }
    }

    override fun run(notifier: RunNotifier?) {
        try {
            super.run(notifier)
        } finally {
            reportGenerator.generate(getChildren().map { it.getFeatureReportDetails() }.toList())
            skelligTestContext!!.close()
        }
    }

    override fun getChildren(): List<FeatureRunner> {
        return children
    }

    override fun describeChild(child: FeatureRunner): Description? {
        return child.description
    }

    override fun runChild(child: FeatureRunner, notifier: RunNotifier?) {
        child.run(notifier)
    }

    override fun childrenInvoker(notifier: RunNotifier?): Statement? {
        val runFeatures = super.childrenInvoker(notifier)
        return RunSkellig(runFeatures)
    }

    private fun getConfig(config: String): String {
        val key = config.substringAfter("\${").substringBefore("}")
        return if (key.isNotEmpty()) {
            val property = System.getProperty(key, "")
            config.replace("\${$key}", property)
        } else {
            config
        }
    }

    private class RunSkellig(private val runFeatures: Statement) : Statement() {
        @Throws(Throwable::class)
        override fun evaluate() {
            runFeatures.evaluate()
        }
    }
}