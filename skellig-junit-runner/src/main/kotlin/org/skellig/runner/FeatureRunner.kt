package org.skellig.runner

import org.junit.runner.Description
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier
import org.junit.runners.model.InitializationError
import org.skellig.feature.Feature
import org.skellig.feature.hook.SkelligHookRunner
import org.skellig.feature.hook.annotation.AfterTestFeature
import org.skellig.feature.hook.annotation.BeforeTestFeature
import org.skellig.feature.metadata.TagsFilter
import org.skellig.runner.exception.FeatureRunnerException
import org.skellig.runner.junit.report.TestStepLogger
import org.skellig.runner.junit.report.model.FeatureReportDetails
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.runner.TestStepRunner

open class FeatureRunner(
    feature: Feature,
    protected val testStepRunner: TestStepRunner?,
    protected val testScenarioState: TestScenarioState?,
    protected val tagsFilter: TagsFilter,
    hookRunner: SkelligHookRunner,
    testStepLogger: TestStepLogger
) : BaseSkelligTestEntityRunner<TestScenarioRunner>(
    feature, hookRunner, testStepLogger,
    BeforeTestFeature::class.java, AfterTestFeature::class.java
) {

    private var description: Description? = null
    private var testScenarioRunners: List<TestScenarioRunner>? = null

    init {
        testScenarioRunners =
            feature.scenarios
                ?.filter { tagsFilter.checkTagsAreIncluded(it.tags) }
                ?.map { TestScenarioRunner.create(it, testStepRunner, hookRunner, testStepLogger) }
                ?.toList() ?: emptyList()
    }

    override fun getDescription(): Description? {
        if (description == null) {
            description = Description.createSuiteDescription(name, name)
            children?.forEach { description!!.addChild(describeChild(it)) }
        }
        return description
    }

    fun getFeatureReportDetails(): FeatureReportDetails {
        val featureReportDetails = FeatureReportDetails(
            name,
            getEntityTags(),
            beforeHookReportDetails,
            afterHookReportDetails,
            children?.map { it.getTestScenarioReportDetails() }?.toList() ?: emptyList()
        )
        return featureReportDetails
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
        } finally {
            notifier.fireTestFinished(childDescription)
            testScenarioState?.clean()
        }
    }

    companion object {
        fun create(
            feature: Feature,
            testStepRunner: TestStepRunner?,
            testScenarioState: TestScenarioState?,
            testStepLogger: TestStepLogger,
            hookRunner: SkelligHookRunner,
            tagsFilter: TagsFilter
        ): FeatureRunner {
            return try {
                FeatureRunner(feature, testStepRunner, testScenarioState, tagsFilter, hookRunner, testStepLogger)
            } catch (e: InitializationError) {
                throw FeatureRunnerException(e.message, e)
            }
        }
    }
}