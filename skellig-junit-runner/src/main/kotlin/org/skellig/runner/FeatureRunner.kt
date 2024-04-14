package org.skellig.runner

import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import org.skellig.feature.Feature
import org.skellig.feature.TestScenario
import org.skellig.feature.TestStep
import org.skellig.feature.hook.SkelligHookRunner
import org.skellig.feature.hook.annotation.AfterTestFeature
import org.skellig.feature.hook.annotation.BeforeTestFeature
import org.skellig.feature.metadata.TagsFilter
import org.skellig.runner.junit.report.TestStepLogger
import org.skellig.runner.junit.report.model.FeatureReportDetails
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.runner.TestStepRunner

private const val BEFORE_FEATURE_NAME = "Before Feature"
private const val AFTER_FEATURE_NAME = "After Feature"

open class FeatureRunner(
    feature: Feature,
    protected val testScenarioState: TestScenarioState?,
    protected val tagsFilter: TagsFilter,
    hookRunner: SkelligHookRunner,
    testStepRunner: TestStepRunner?,
    testStepLogger: TestStepLogger
) : BaseSkelligTestEntityRunner<TestScenarioRunner>(
    feature, hookRunner, testStepRunner, testStepLogger,
    BeforeTestFeature::class.java, AfterTestFeature::class.java
) {

    private var description: Description? = null
    private var testScenarioRunners: MutableList<TestScenarioRunner>? = null

    override fun getDescription(): Description {
        if (description == null) {
            description = Description.createSuiteDescription(name, getId())
            children?.forEach { description!!.addChild(describeChild(it)) }
        }
        return description ?: error("Failed to create description of feature: " + testEntity.getEntityName())
    }

    override fun getChildren(): List<TestScenarioRunner>? {
        if (testScenarioRunners == null) {
            testScenarioRunners = mutableListOf()
            val feature = testEntity as Feature

            feature.beforeSteps?.let {
                testScenarioRunners!!.add(
                    TestScenarioRunner.create(
                        TestScenarioWrapper(feature.filePath, getBeforeFeatureName(), it, null),
                        testStepRunner, hookRunner, testStepLogger
                    )
                )
            }

            feature.scenarios
                ?.filter { tagsFilter.checkTagsAreIncluded(it.tags) }
                ?.forEach { testScenarioRunners!!.add(TestScenarioRunner.create(it, testStepRunner, hookRunner, testStepLogger)) }

            feature.afterSteps?.let {
                testScenarioRunners!!.add(
                    TestScenarioRunner.create(
                        TestScenarioWrapper(feature.filePath, getAfterFeatureName(), null, it),
                        testStepRunner, hookRunner, testStepLogger
                    )
                )
            }
        }
        return testScenarioRunners
    }

    override fun describeChild(child: TestScenarioRunner): Description {
        return child.description
    }

    override fun runChild(child: TestScenarioRunner, notifier: RunNotifier) {
        try {
            log.info("Run Test Scenario '${child}'")
            child.run(notifier)
            log.info("Test Scenario '${child}' has finished")
        } finally {
            log.info("Cleanup the Test Scenario State")
            testScenarioState?.clean()
        }
    }

    fun getFeatureReportDetails(): FeatureReportDetails {
        val featureReportDetails = FeatureReportDetails(
            name,
            getEntityTags(),
            beforeHookReportDetails,
            afterHookReportDetails,
            children?.find { it.getEntityName() == getBeforeFeatureName() }?.getTestScenarioReportDetails()?.beforeReportDetails,
            children?.find { it.getEntityName() == getAfterFeatureName() }?.getTestScenarioReportDetails()?.afterReportDetails,
            children
                ?.filter { it.getEntityName() != getBeforeFeatureName() && it.getEntityName() != getAfterFeatureName() }
                ?.map { it.getTestScenarioReportDetails() }?.toList() ?: emptyList()
        )
        return featureReportDetails
    }

    private fun getBeforeFeatureName() = "$name:$BEFORE_FEATURE_NAME"

    private fun getAfterFeatureName() = "$name:$AFTER_FEATURE_NAME"

    companion object {
        fun create(
            feature: Feature,
            testStepRunner: TestStepRunner?,
            testScenarioState: TestScenarioState?,
            testStepLogger: TestStepLogger,
            hookRunner: SkelligHookRunner,
            tagsFilter: TagsFilter
        ): FeatureRunner {
            return FeatureRunner(feature, testScenarioState, tagsFilter, hookRunner, testStepRunner, testStepLogger)
        }
    }

    /**
     * A wrapper for TestScenario which is used to group before and after test steps of the feature
     */
    class TestScenarioWrapper(
        path: String,
        name: String,
        beforeSteps: List<TestStep>?,
        afterSteps: List<TestStep>?
    ) : TestScenario(path, name, null, null, beforeSteps, afterSteps)
}