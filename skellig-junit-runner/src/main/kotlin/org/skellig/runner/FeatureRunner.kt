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
import org.skellig.plugin.report.TestStepLogger
import org.skellig.teststep.processing.state.ParallelGroupContext
import org.skellig.teststep.processing.state.TestScenarioScope
import org.skellig.teststep.runner.TestStepRunner

private const val BEFORE_FEATURE_NAME = "Before Feature"
private const val AFTER_FEATURE_NAME = "After Feature"

/**
 * FeatureRunner is responsible for running the test scenarios within a [Feature].
 *
 * @property feature The feature being executed
 * @property tagsFilter The tags filter to determine which test scenarios to include/exclude based on their tags
 * @property hookRunner The hook runner to run hooks before and after feature execution
 * @property testStepRunner The test step runner to run individual test steps
 * @property testStepLogger The test step logger for logging test steps
 * @property scope The scope the hooks of the feature, and its steps before and after the feature, run in. Without a
 * [scenarioScopeFactory], also the scope the steps of its scenarios run in, and its state, which holds data passed through
 * test steps during a test scenario run, is cleaned after each [TestScenarioRunner] is finished.
 * @property scenarioScopeFactory Creates the scope of a scenario from the id of the scope. It is set when the feature is
 * a parallel group of its own: every scenario then runs in its own scope with a state of its own, which is dropped when
 * the scenario has finished, and the instances of the [ParallelGroupContext] of [scope], which all the scopes must refer
 * to, are dropped when the feature has finished.
 */
open class FeatureRunner(
    feature: Feature,
    protected val tagsFilter: TagsFilter,
    hookRunner: SkelligHookRunner,
    testStepRunner: TestStepRunner?,
    testStepLogger: TestStepLogger,
    eventDispatcher: SkelligTestEventDispatcher,
    scope: TestScenarioScope,
    private val scenarioScopeFactory: ((String) -> TestScenarioScope)? = null
) : BaseSkelligTestEntityRunner<TestScenarioRunner>(
    feature, hookRunner, testStepRunner, testStepLogger,
    BeforeTestFeature::class.java, AfterTestFeature::class.java, eventDispatcher, scope
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
                        TestScenarioWrapper(feature.filePath + ":$BEFORE_FEATURE_NAME", BEFORE_FEATURE_NAME, it, null),
                        testEntity.getId(), testStepRunner, hookRunner, testStepLogger, eventDispatcher, scope
                    )
                )
            }

            feature.scenarios
                ?.filter { tagsFilter.checkTagsAreIncluded(it.tags) }
                ?.forEach {
                    testScenarioRunners!!.add(
                        TestScenarioRunner.create(
                            it, testEntity.getId(), testStepRunner, hookRunner, testStepLogger, eventDispatcher, scopeOf(it)
                        )
                    )
                }

            feature.afterSteps?.let {
                testScenarioRunners!!.add(
                    TestScenarioRunner.create(
                        TestScenarioWrapper(feature.filePath + ":$AFTER_FEATURE_NAME", AFTER_FEATURE_NAME, null, it),
                        testEntity.getId(), testStepRunner, hookRunner, testStepLogger, eventDispatcher, scope
                    )
                )
            }
        }
        return testScenarioRunners
    }

    /**
     * The scope the steps of the [scenario] run in: a new one of its own if the feature creates them, otherwise the one
     * of the feature.
     */
    private fun scopeOf(scenario: TestScenario): TestScenarioScope =
        scenarioScopeFactory?.invoke("${testEntity.getEntityName()}/${scenario.name}#${Integer.toHexString(scenario.getId())}") ?: scope

    override fun describeChild(child: TestScenarioRunner): Description {
        return child.description
    }

    override fun runChild(child: TestScenarioRunner, notifier: RunNotifier) {
        try {
            child.run(notifier)
        } finally {
            if (scenarioScopeFactory == null) {
                log.info("Cleanup the Test Scenario State")
                scope.state.clean()
            } else {
                // the state of a scenario of its own is not needed once the scenario has finished
                child.scenarioScope.takeIf { it !== scope }?.state?.clean()
            }
        }
    }

    override fun run(notifier: RunNotifier) {
        try {
            eventDispatcher.dispatch(FeatureStartedEvent(testEntity as Feature))
            super.run(notifier)
        } finally {
            if (scenarioScopeFactory != null) scope.parallelGroup.close()
            eventDispatcher.dispatch(FeatureFinishedEvent(testEntity.getId()))
        }
    }

    override fun dispatchHookFinishedEvent(hookName: String, duration: Long, e: Throwable?, hookType: Class<out Annotation>) {
        eventDispatcher.dispatch(
            HookFinishedEvent(
                hookName, testEntity.getId(), null, testStepLogger.getLogsAndClean(scope.id), Result(duration, e, null),
                if (hookType == BeforeTestFeature::class.java) ExecutionSequenceType.BEFORE else ExecutionSequenceType.AFTER
            )
        )
    }

    companion object {
        fun create(
            feature: Feature,
            testStepRunner: TestStepRunner?,
            testStepLogger: TestStepLogger,
            hookRunner: SkelligHookRunner,
            eventDispatcher: SkelligTestEventDispatcher,
            tagsFilter: TagsFilter,
            scope: TestScenarioScope,
            scenarioScopeFactory: ((String) -> TestScenarioScope)? = null
        ): FeatureRunner {
            return FeatureRunner(feature, tagsFilter, hookRunner, testStepRunner, testStepLogger, eventDispatcher, scope, scenarioScopeFactory)
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