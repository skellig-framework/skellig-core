package org.skellig.runner

import org.junit.runner.notification.RunNotifier
import org.junit.runners.model.Statement
import org.skellig.feature.Feature
import org.skellig.feature.event.SkelligTestEventDispatcher
import org.skellig.feature.hook.SkelligHookRunner
import org.skellig.feature.metadata.TagsFilter
import org.skellig.plugin.report.TestStepLogger
import org.skellig.runner.parallel.ParallelGroupScheduler
import org.skellig.teststep.processing.state.ParallelGroupContext
import org.skellig.teststep.processing.state.TestScenarioScope
import org.skellig.teststep.runner.TestStepRunner

/**
 * Runs the test scenarios within a [Feature] in parallel, every scenario as a parallel group of its own.
 *
 * The setup of the feature (its hooks and its steps before the feature) runs once before any of its scenarios, and its
 * teardown (its steps and hooks after the feature) runs once when all of them have finished, both on the thread
 * which runs the feature and in the [scope] of the feature. So the scenarios never share instances of user classes or
 * state with the setup, with the teardown or with each other, while the teardown sees what the setup has left.
 *
 * @property scope The scope the hooks of the feature, and its steps before and after the feature, run in.
 * @property scenarioScopeFactory Creates the scope of a scenario from the id of the scope, with a state and a
 * [ParallelGroupContext] of its own, which are both dropped when the scenario has finished.
 * @property scenarioSchedulerFactory Creates the scheduler which runs the scenarios of the feature in parallel.
 */
class ParallelFeatureRunner(
    feature: Feature,
    tagsFilter: TagsFilter,
    hookRunner: SkelligHookRunner,
    testStepRunner: TestStepRunner?,
    testStepLogger: TestStepLogger,
    eventDispatcher: SkelligTestEventDispatcher,
    scope: TestScenarioScope,
    scenarioScopeFactory: (String) -> TestScenarioScope,
    private val scenarioSchedulerFactory: () -> ParallelGroupScheduler
) : BaseFeatureRunner(feature, tagsFilter, hookRunner, testStepRunner, testStepLogger, eventDispatcher, scope, scenarioScopeFactory) {

    override fun childrenInvoker(notifier: RunNotifier): Statement {
        return object : Statement() {
            override fun evaluate() {
                beforeFeatureSteps?.run(notifier)

                val scheduler = scenarioSchedulerFactory()
                try {
                    testScenarios.forEach { testScenario -> scheduler.schedule { runChild(testScenario, notifier) } }
                } finally {
                    // waits for all the scenarios and reports the first unexpected exception
                    scheduler.finished()
                }

                afterFeatureSteps?.run(notifier)
            }
        }
    }

    override fun runChild(child: TestScenarioRunner, notifier: RunNotifier) {
        try {
            child.run(notifier)
        } finally {
            // the scenario is a parallel group of its own, so nothing of it is needed once it has finished
            child.scenarioScope.state.clean()
            child.scenarioScope.parallelGroup.close()
        }
    }
}
