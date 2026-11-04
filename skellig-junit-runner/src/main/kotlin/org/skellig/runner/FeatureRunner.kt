package org.skellig.runner

import org.junit.internal.runners.model.EachTestNotifier
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import org.junit.runner.notification.StoppedByUserException
import org.skellig.feature.Feature
import org.skellig.feature.TestScenario
import org.skellig.feature.TestStep
import org.skellig.feature.event.*
import org.skellig.feature.hook.SkelligHookRunner
import org.skellig.feature.hook.annotation.AfterTestFeature
import org.skellig.feature.hook.annotation.BeforeTestFeature
import org.skellig.feature.metadata.TagsFilter
import org.skellig.plugin.report.TestStepLogger
import org.skellig.runner.parallel.FeatureGate
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
 * @property scenarioScopeFactory Creates the scope of a scenario from the id of the scope. It is set when the scenarios
 * of the feature run in parallel: every scenario then runs in its own scope with a state of its own, which is dropped
 * when the scenario has finished. If the feature is a parallel group of its own, then the scopes refer to the
 * [ParallelGroupContext] of [scope], and its instances are dropped when the feature has finished. If every scenario
 * is a parallel group of its own, then each scope refers to a [ParallelGroupContext] of its own, which is dropped
 * when the scenario has finished, and the one of [scope] belongs to the setup and the teardown of the feature only.
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

    private var featureDescription: Description? = null
    private var allRunners: List<TestScenarioRunner>? = null
    private var beforeFeatureRunner: TestScenarioRunner? = null
    private var afterFeatureRunner: TestScenarioRunner? = null
    private var scenarios: List<TestScenarioRunner> = emptyList()

    // the setup and the teardown of the feature can be run by different threads, but never at the same time
    @Volatile
    private var suiteNotifier: EachTestNotifier? = null
    private val gate by lazy { FeatureGate(scenarioRunners.size) }

    /**
     * The runners of the scenarios of the feature which are not excluded by tags, in the order of the feature,
     * without the steps before and after the feature.
     */
    internal val scenarioRunners: List<TestScenarioRunner>
        get() {
            createRunners()
            return scenarios
        }

    @Synchronized
    override fun getDescription(): Description {
        if (featureDescription == null) {
            featureDescription = Description.createSuiteDescription(name, getId())
            children?.forEach { featureDescription!!.addChild(describeChild(it)) }
        }
        return featureDescription ?: error("Failed to create description of feature: " + testEntity.getEntityName())
    }

    override fun getChildren(): List<TestScenarioRunner>? {
        createRunners()
        return allRunners
    }

    /**
     * Creates the runners of the steps before the feature, of its scenarios and of the steps after the feature once.
     * It can be called from the threads of different parallel groups.
     */
    @Synchronized
    private fun createRunners() {
        if (allRunners == null) {
            val feature = testEntity as Feature

            beforeFeatureRunner = feature.beforeSteps?.let {
                TestScenarioRunner.create(
                    TestScenarioWrapper(feature.filePath + ":$BEFORE_FEATURE_NAME", BEFORE_FEATURE_NAME, it, null),
                    testEntity.getId(), testStepRunner, hookRunner, testStepLogger, eventDispatcher, scope
                )
            }

            scenarios = feature.scenarios
                ?.filter { tagsFilter.checkTagsAreIncluded(it.tags) }
                ?.map {
                    TestScenarioRunner.create(
                        it, testEntity.getId(), testStepRunner, hookRunner, testStepLogger, eventDispatcher, scopeOf(it)
                    )
                } ?: emptyList()

            afterFeatureRunner = feature.afterSteps?.let {
                TestScenarioRunner.create(
                    TestScenarioWrapper(feature.filePath + ":$AFTER_FEATURE_NAME", AFTER_FEATURE_NAME, null, it),
                    testEntity.getId(), testStepRunner, hookRunner, testStepLogger, eventDispatcher, scope
                )
            }

            allRunners = listOfNotNull(beforeFeatureRunner) + scenarios + listOfNotNull(afterFeatureRunner)
        }
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
                val scenarioScope = child.scenarioScope
                if (scenarioScope !== scope) {
                    // the state of a scenario of its own is not needed once the scenario has finished
                    scenarioScope.state.clean()
                    // and neither are the instances of a scenario which is a parallel group of its own
                    if (scenarioScope.parallelGroup !== scope.parallelGroup) scenarioScope.parallelGroup.close()
                }
            }
        }
    }

    /**
     * Runs the whole feature: its setup, all its scenarios one after another, and its teardown.
     * It must not be used together with [runScenario].
     */
    override fun run(notifier: RunNotifier) {
        try {
            eventDispatcher.dispatch(FeatureStartedEvent(testEntity as Feature))
            super.run(notifier)
        } finally {
            if (scenarioScopeFactory != null) scope.parallelGroup.close()
            eventDispatcher.dispatch(FeatureFinishedEvent(testEntity.getId()))
        }
    }

    /**
     * Runs one [scenario] of the feature, which must be one of [scenarioRunners], when the scenarios of the feature run in
     * different parallel groups. All the scenarios of the feature must be run this way, and the feature must not be
     * run as a whole with [run].
     *
     * The setup of the feature, that is the suite started, the hooks before the feature and the steps before it, runs
     * once, when the first scenario is reached by any parallel group, while the other parallel groups wait for it.
     * The scenario runs after the setup, unless the setup has failed with a hook, which skips all scenarios of the
     * feature. The teardown of the feature, that is the steps after it, the hooks after it, waiting for the results of
     * asynchronous steps, and the suite finished, runs once, when the last scenario of the feature is done, whether
     * it has run, has failed or has been skipped. After a failure of the setup, the teardown only closes the suite.
     *
     * The setup and the teardown run in the scope of the feature, and in its parallel group,
     * whichever thread runs them.
     */
    internal fun runScenario(scenario: TestScenarioRunner, notifier: RunNotifier) {
        try {
            if (gate.enter { setUp(notifier) }) runChild(scenario, notifier)
        } finally {
            if (gate.leave()) tearDown(notifier)
        }
    }

    private fun setUp(notifier: RunNotifier): Boolean {
        eventDispatcher.dispatch(FeatureStartedEvent(testEntity as Feature))
        val testNotifier = EachTestNotifier(notifier, getDescription())
        testNotifier.fireTestSuiteStarted()
        suiteNotifier = testNotifier
        return try {
            runBeforeHooks(notifier)
            beforeFeatureRunner?.let { runChild(it, notifier) }
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
            // after a failing hook before the feature, the steps and hooks after it do not run, as the feature has not started
            if (testNotifier != null && gate.isSetupSucceeded) {
                try {
                    afterFeatureRunner?.let { runChild(it, notifier) }
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