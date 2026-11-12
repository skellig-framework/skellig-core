package org.skellig.runner

import org.junit.internal.runners.model.EachTestNotifier
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import org.junit.runner.notification.StoppedByUserException
import org.skellig.feature.Feature
import org.skellig.feature.event.*
import org.skellig.feature.hook.SkelligHookRunner
import org.skellig.feature.hook.annotation.AfterTestFeature
import org.skellig.feature.hook.annotation.BeforeTestFeature
import org.skellig.feature.metadata.TagsFilter
import org.skellig.plugin.report.TestStepLogger
import org.skellig.runner.parallel.FeatureGate
import org.skellig.runner.parallel.ParallelGroup
import org.skellig.teststep.processing.state.TestScenarioScope
import org.skellig.teststep.runner.TestStepRunner

/**
 * ScenarioOfFeatureRunner is responsible for running the test scenarios within a [Feature] when each of them is
 * a parallel group of its own, so the scenarios of the feature can run at the same time on different threads.
 *
 * The setup and the teardown of the feature must still run once, so every scenario is run through a gate of the feature
 * with [runChild]. The first scenario which is run runs the setup, which is the suite started, the hooks before the feature
 * and the steps before it, while the other scenarios wait for it, and no scenario starts before it has finished.
 * The scenario that is the last one of the feature to finish runs the teardown, which is the steps after the feature, the hooks
 * after it, waiting for the results of asynchronous steps, and the suite finished, whether the scenarios have run, have failed, or
 * have been skipped. A failing hook before the feature skips all the scenarios, and then the teardown only closes the suite.
 *
 * The setup and the teardown run in the [scope] of the feature, and in its parallel group, whichever thread runs them.
 *
 * @property feature The feature being executed
 * @property tagsFilter The tags filter to determine which test scenarios to include/exclude based on their tags
 * @property scope The scope the hooks of the feature, and its steps before and after the feature, run in.
 * Its parallel group is dropped when the feature has finished.
 * @property scenarioScopeFactory Creates the scope of a scenario from the id of the scope. Every scenario runs in a scope
 * with a state and a parallel group of its own, which are dropped when the scenario has finished.
 */
open class ScenarioOfFeatureRunner(
    private val feature: Feature,
    private val tagsFilter: TagsFilter,
    hookRunner: SkelligHookRunner,
    testStepRunner: TestStepRunner?,
    testStepLogger: TestStepLogger,
    eventDispatcher: SkelligTestEventDispatcher,
    scope: TestScenarioScope,
    private val scenarioScopeFactory: (String) -> TestScenarioScope
) : BaseSkelligTestEntityRunner<TestScenarioRunner>(
    feature, hookRunner, testStepRunner, testStepLogger,
    BeforeTestFeature::class.java, AfterTestFeature::class.java, eventDispatcher, scope
), SkelligFeatureRunner {

    // the runners are created by the first thread which needs them, as they are used from the threads of different parallel groups
    private val beforeFeatureRunner: TestScenarioRunner? by lazy {
        feature.beforeSteps?.let {
            TestScenarioRunner.create(
                FeatureRunner.TestScenarioWrapper(feature.filePath + ":$BEFORE_FEATURE_NAME", BEFORE_FEATURE_NAME, it, null),
                feature.getId(), testStepRunner, hookRunner, testStepLogger, eventDispatcher, scope
            )
        }
    }

    private val scenarioRunners: List<TestScenarioRunner> by lazy {
        feature.scenarios
            ?.filter { tagsFilter.checkTagsAreIncluded(it.tags) }
            ?.map {
                val scenarioScopeId = "${feature.getEntityName()}/${it.name}#${Integer.toHexString(it.getId())}"
                TestScenarioRunner.create(
                    it, feature.getId(), testStepRunner, hookRunner, testStepLogger, eventDispatcher, scenarioScopeFactory(scenarioScopeId)
                )
            } ?: emptyList()
    }

    private val afterFeatureRunner: TestScenarioRunner? by lazy {
        feature.afterSteps?.let {
            TestScenarioRunner.create(
                FeatureRunner.TestScenarioWrapper(feature.filePath + ":$AFTER_FEATURE_NAME", AFTER_FEATURE_NAME, null, it),
                feature.getId(), testStepRunner, hookRunner, testStepLogger, eventDispatcher, scope
            )
        }
    }

    private val featureDescription: Description by lazy {
        Description.createSuiteDescription(name, getId()).also { description ->
            (listOfNotNull(beforeFeatureRunner) + scenarioRunners + listOfNotNull(afterFeatureRunner))
                .forEach { description.addChild(describeChild(it)) }
        }
    }

    private val gate by lazy { FeatureGate(scenarioRunners.size) }

    // the setup and the teardown of the feature can be run by different threads, but never at the same time
    @Volatile
    private var suiteNotifier: EachTestNotifier? = null

    override fun getDescription(): Description = featureDescription

    /**
     * The scenarios of the feature which are not excluded by tags, in the order of the feature, without the steps
     * before and after the feature.
     */
    override fun getChildren(): List<TestScenarioRunner> = scenarioRunners

    override fun describeChild(child: TestScenarioRunner): Description = child.description

    /**
     * Runs the whole feature on the calling thread, one scenario after another.
     * If there are no scenarios to run, then there is nothing to set up or to tear down, so nothing runs.
     */
    override fun run(notifier: RunNotifier) {
        scenarioRunners.forEach { runChild(it, notifier) }
    }

    /**
     * Every scenario is a parallel group.
     */
    override fun createParallelGroups(): List<ParallelGroup> {
        // the descriptions of all the runners of the feature are created before they run in different threads
        getDescription()
        return scenarioRunners.map { scenario ->
            ParallelGroup("${getEntityName()}/${scenario.getEntityName()}", listOf { notifier -> runChild(scenario, notifier) })
        }
    }

    override fun runChild(child: TestScenarioRunner, notifier: RunNotifier) {
        try {
            if (gate.enter { setUp(notifier) }) {
                try {
                    child.run(notifier)
                } finally {
                    // a scenario of its own does not need its state, nor its instances, once it has finished
                    child.scenarioScope.state.clean()
                    child.scenarioScope.parallelGroup.close()
                }
            }
        } finally {
            if (gate.leave()) tearDown(notifier)
        }
    }

    private fun setUp(notifier: RunNotifier): Boolean {
        eventDispatcher.dispatch(FeatureStartedEvent(feature))
        val testNotifier = EachTestNotifier(notifier, description)
        testNotifier.fireTestSuiteStarted()
        suiteNotifier = testNotifier
        return try {
            runBeforeHooks(notifier)
            beforeFeatureRunner?.run(notifier)
            true
        } catch (e: StoppedByUserException) {
            throw e
        } catch (e: Throwable) {
            testNotifier.addFailure(e)
            false
        }
    }

    private fun tearDown(notifier: RunNotifier) {
        val testNotifier = suiteNotifier
        try {
            // the steps and hooks after the feature do not run if it has not started, which is also if the setup has not even begun
            if (testNotifier != null && gate.isSetupSucceeded) {
                try {
                    afterFeatureRunner?.run(notifier)
                    runAfterHooks(notifier)
                } catch (e: StoppedByUserException) {
                    throw e
                } catch (e: Throwable) {
                    testNotifier.addFailure(e)
                }
            }
        } finally {
            try {
                awaitForTestStepRunResults(testStepRunResults, notifier)
                testNotifier?.fireTestSuiteFinished()
            } finally {
                scope.parallelGroup.close()
                eventDispatcher.dispatch(FeatureFinishedEvent(testEntity.getId()))
            }
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
}
