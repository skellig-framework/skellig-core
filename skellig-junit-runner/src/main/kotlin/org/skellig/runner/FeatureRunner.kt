package org.skellig.runner

import org.junit.runner.notification.RunNotifier
import org.skellig.feature.Feature
import org.skellig.feature.event.SkelligTestEventDispatcher
import org.skellig.feature.hook.SkelligHookRunner
import org.skellig.feature.metadata.TagsFilter
import org.skellig.plugin.report.TestStepLogger
import org.skellig.teststep.processing.state.ParallelGroupContext
import org.skellig.teststep.processing.state.TestScenarioScope
import org.skellig.teststep.runner.TestStepRunner

/**
 * FeatureRunner is responsible for running the test scenarios within a [Feature], one after another.
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
    tagsFilter: TagsFilter,
    hookRunner: SkelligHookRunner,
    testStepRunner: TestStepRunner?,
    testStepLogger: TestStepLogger,
    eventDispatcher: SkelligTestEventDispatcher,
    scope: TestScenarioScope,
    scenarioScopeFactory: ((String) -> TestScenarioScope)? = null
) : BaseFeatureRunner(feature, tagsFilter, hookRunner, testStepRunner, testStepLogger, eventDispatcher, scope, scenarioScopeFactory) {

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
}
