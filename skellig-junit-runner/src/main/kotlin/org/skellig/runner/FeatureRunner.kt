package org.skellig.runner

import com.typesafe.config.Config
import org.junit.runner.Description
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier
import org.junit.runners.ParentRunner
import org.junit.runners.model.InitializationError
import org.skellig.feature.Feature
import org.skellig.feature.metadata.TagsFilter
import org.skellig.runner.exception.FeatureRunnerException
import org.skellig.runner.junit.report.model.FeatureReportDetails
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.runner.TestStepRunner

open class FeatureRunner(
    val feature: Feature,
    protected val testStepRunner: TestStepRunner?,
    protected val testScenarioState: TestScenarioState?,
    config: Config?,
    protected val tagsFilter: TagsFilter
) : ParentRunner<TestScenarioRunner>(feature.javaClass) {

    private var description: Description? = null
    private var testScenarioRunners: List<TestScenarioRunner>? = null

    init {
        testScenarioRunners =
            feature.scenarios
                ?.filter { tagsFilter.checkTagsAreIncluded(it.tags) }
                ?.map { TestScenarioRunner.create(it, testStepRunner, config) }
                ?.toList() ?: emptyList()
    }

    override fun getDescription(): Description? {
        if (description == null) {
            description = Description.createSuiteDescription(name, feature.name)
            children?.forEach { description!!.addChild(describeChild(it)) }
        }
        return description
    }

    fun getFeatureReportDetails(): FeatureReportDetails {
        return FeatureReportDetails(
            name,
            feature.tags,
            children?.map { it.getTestScenarioReportDetails() }?.toList() ?: emptyList()
        )
    }

    override fun getName(): String {
        return feature.name
    }

    override fun getChildren(): List<TestScenarioRunner>? {
        return testScenarioRunners
    }

    override fun describeChild(child: TestScenarioRunner): Description {
        return child.description
    }

    override fun runChild(child: TestScenarioRunner, notifier: RunNotifier) {
        val childDescription = describeChild(child)
        notifier.fireTestStarted(childDescription)
        try {
            child.run(notifier)
        } catch (e: Throwable) {
            notifier.fireTestFailure(Failure(childDescription, e))
            notifier.pleaseStop()
        } finally {
            notifier.fireTestFinished(childDescription)
            testScenarioState!!.clean()
        }
    }

    companion object {
        fun create(
            feature: Feature,
            testStepRunner: TestStepRunner?,
            testScenarioState: TestScenarioState?,
            config: Config?,
            tagsFilter: TagsFilter
        ): FeatureRunner {
            return try {
                FeatureRunner(feature, testStepRunner, testScenarioState, config, tagsFilter)
            } catch (e: InitializationError) {
                throw FeatureRunnerException(e.message, e)
            }
        }
    }
}