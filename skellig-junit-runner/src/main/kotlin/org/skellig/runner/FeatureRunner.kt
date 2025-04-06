package org.skellig.runner

import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import org.skellig.feature.Feature
import org.skellig.feature.TestScenario
import org.skellig.feature.TestStep
import org.skellig.feature.event.*
import org.skellig.feature.hook.SkelligHookRunner
import org.skellig.feature.hook.annotation.AfterTestFeature
import org.skellig.feature.hook.annotation.BeforeTestFeature
import org.skellig.feature.metadata.TagsFilter
import org.skellig.runner.junit.report.TestStepLogger
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.runner.TestStepRunner

private const val BEFORE_FEATURE_NAME = "Before Feature"
private const val AFTER_FEATURE_NAME = "After Feature"

/**
 * FeatureRunner is responsible for running the test scenarios within a [Feature].
 *
 * @property feature The feature being executed
 * @property testScenarioState The test scenario state with data passed through test steps during a test scenario run.
 * It removes all data after each [TestScenarioRunner] is finished.
 * @property tagsFilter The tags filter to determine which test scenarios to include/exclude based on their tags
 * @property hookRunner The hook runner to run hooks before and after feature execution
 * @property testStepRunner The test step runner to run individual test steps
 * @property testStepLogger The test step logger for logging test steps
 */
open class FeatureRunner(
    feature: Feature,
    protected val testScenarioState: TestScenarioState?,
    protected val tagsFilter: TagsFilter,
    hookRunner: SkelligHookRunner,
    testStepRunner: TestStepRunner?,
    testStepLogger: TestStepLogger,
    eventDispatcher: SkelligTestEventDispatcher
) : BaseSkelligTestEntityRunner<TestScenarioRunner>(
    feature, hookRunner, testStepRunner, testStepLogger,
    BeforeTestFeature::class.java, AfterTestFeature::class.java, eventDispatcher
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
                        testEntity.getId(), testStepRunner, hookRunner, testStepLogger, eventDispatcher
                    )
                )
            }

            feature.scenarios
                ?.filter { tagsFilter.checkTagsAreIncluded(it.tags) }
                ?.forEach { testScenarioRunners!!.add(TestScenarioRunner.create(it, testEntity.getId(), testStepRunner, hookRunner, testStepLogger, eventDispatcher)) }

            feature.afterSteps?.let {
                testScenarioRunners!!.add(
                    TestScenarioRunner.create(
                        TestScenarioWrapper(feature.filePath, getAfterFeatureName(), null, it),
                        testEntity.getId(), testStepRunner, hookRunner, testStepLogger, eventDispatcher
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
            child.run(notifier)
        } finally {
            log.info("Cleanup the Test Scenario State")
            testScenarioState?.clean()
        }
    }

    override fun run(notifier: RunNotifier) {
        try {
            eventDispatcher.dispatch(FeatureStartedEvent(testEntity))
            super.run(notifier)
        } finally {
            eventDispatcher.dispatch(FeatureFinishedEvent(testEntity.getId()))
        }
    }

    private fun getBeforeFeatureName() = "$name:$BEFORE_FEATURE_NAME"

    private fun getAfterFeatureName() = "$name:$AFTER_FEATURE_NAME"

    override fun dispatchHookFinishedEvent(hookName: String, duration: Long, e: Throwable?, hookType: Class<out Annotation>) {
        eventDispatcher.dispatch(HookFinishedEvent(hookName, testEntity.getId(), null, testStepLogger.getLogsAndClean(), Result(duration, e, null),
            if(hookType == BeforeTestFeature::class.java) ExecutionSequenceType.BEFORE else ExecutionSequenceType.AFTER))
    }

    companion object {
        fun create(
            feature: Feature,
            testStepRunner: TestStepRunner?,
            testScenarioState: TestScenarioState?,
            testStepLogger: TestStepLogger,
            hookRunner: SkelligHookRunner,
            eventDispatcher: SkelligTestEventDispatcher,
            tagsFilter: TagsFilter
        ): FeatureRunner {
            return FeatureRunner(feature, testScenarioState, tagsFilter, hookRunner, testStepRunner, testStepLogger, eventDispatcher)
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