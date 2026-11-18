package org.skellig.runner

import org.junit.internal.runners.model.EachTestNotifier
import org.junit.runner.notification.RunNotifier
import org.junit.runner.notification.StoppedByUserException
import org.skellig.feature.Feature
import org.skellig.feature.TestScenario
import org.skellig.feature.event.FeatureStartedEvent
import org.skellig.feature.event.FeatureFinishedEvent
import org.skellig.feature.event.SkelligTestEventDispatcher
import org.skellig.feature.hook.SkelligHookRunner
import org.skellig.feature.metadata.TagsFilter
import org.skellig.plugin.report.TestStepLogger
import org.skellig.teststep.processing.state.TestScenarioScope
import org.skellig.teststep.runner.TestStepRunner
import java.util.concurrent.atomic.AtomicInteger

/**
 * A feature gate for scenarios dispatched by run-wide tag groups. The first group runs setup and the last runs
 * teardown, both in the feature's scope. Waiting for setup never holds a worker needed to execute that setup.
 */
internal class TagFeatureRunner(
    private val feature: Feature,
    tagsFilter: TagsFilter,
    hookRunner: SkelligHookRunner,
    testStepRunner: TestStepRunner,
    testStepLogger: TestStepLogger,
    eventDispatcher: SkelligTestEventDispatcher,
    scope: TestScenarioScope,
    private val createScope: (TestScenario) -> TestScenarioScope
) : BaseFeatureRunner(feature, tagsFilter, hookRunner, testStepRunner, testStepLogger, eventDispatcher, scope, { scope }) {
    val scenarios: List<TestScenarioRunner> get() = testScenarios
    private val remaining by lazy { AtomicInteger(scenarios.size) }
    private var started = false
    private var setupSucceeded = false

    override fun scopeOf(scenario: TestScenario) = createScope(scenario)

    @Synchronized
    private fun start(notifier: RunNotifier): Boolean {
        if (!started) {
            started = true
            eventDispatcher.dispatch(FeatureStartedEvent(feature))
            val events = EachTestNotifier(notifier, description)
            events.fireTestSuiteStarted()
            try {
                runBeforeHooks(notifier)
                beforeFeatureSteps?.run(notifier)
                setupSucceeded = true
            } catch (e: StoppedByUserException) {
                throw e
            } catch (e: Throwable) {
                events.addFailure(e)
            }
        }
        return setupSucceeded
    }

    public override fun runChild(child: TestScenarioRunner, notifier: RunNotifier) {
        try {
            if (start(notifier)) child.run(notifier)
        } finally {
            child.scenarioScope.state.clean()
            if (remaining.decrementAndGet() == 0) finish(notifier)
        }
    }

    // A feature with no selected scenarios still has its normal feature lifecycle.
    override fun run(notifier: RunNotifier) {
        try { start(notifier) } finally { finish(notifier) }
    }

    private fun finish(notifier: RunNotifier) {
        val events = EachTestNotifier(notifier, description)
        try {
            if (setupSucceeded) {
                afterFeatureSteps?.run(notifier)
                runAfterHooks(notifier)
            }
        } catch (e: StoppedByUserException) {
            throw e
        } catch (e: Throwable) {
            events.addFailure(e)
        } finally {
            try {
                awaitForTestStepRunResults(testStepRunResults, notifier)
                events.fireTestSuiteFinished()
            } finally {
                scope.state.clean()
                scope.parallelGroup.close()
                eventDispatcher.dispatch(FeatureFinishedEvent(feature.getId()))
            }
        }
    }
}
