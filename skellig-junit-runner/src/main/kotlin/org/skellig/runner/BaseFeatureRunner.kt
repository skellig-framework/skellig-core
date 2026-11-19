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
 * What the runners of a [Feature] share: the runners of its steps before the feature, of its test scenarios and of
 * its steps after the feature, its description, and the events and hooks which surround its run.
 *
 * @property tagsFilter The tags filter to determine which test scenarios to include/exclude based on their tags
 * @property scope The scope the hooks of the feature, and its steps before and after the feature, run in. Without a
 * [scenarioScopeFactory], also the scope the steps of its scenarios run in.
 * @property scenarioScopeFactory Creates the scope of a scenario from the id of the scope. It is set in a parallel run:
 * every scenario then runs in its own scope with a state of its own, and the instances of the [ParallelGroupContext]
 * of [scope] are dropped when the feature has finished.
 */
abstract class BaseFeatureRunner(
    feature: Feature,
    protected val tagsFilter: TagsFilter,
    hookRunner: SkelligHookRunner,
    testStepRunner: TestStepRunner?,
    testStepLogger: TestStepLogger,
    eventDispatcher: SkelligTestEventDispatcher,
    scope: TestScenarioScope,
    protected val scenarioScopeFactory: ((String) -> TestScenarioScope)?
) : BaseSkelligTestEntityRunner<TestScenarioRunner>(
    feature, hookRunner, testStepRunner, testStepLogger,
    BeforeTestFeature::class.java, AfterTestFeature::class.java, eventDispatcher, scope
) {

    private var description: Description? = null

    /**
     * Runs the steps before the feature, if it has any, in the [scope] of the feature.
     */
    protected val beforeFeatureSteps: TestScenarioRunner? by lazy {
        feature.beforeSteps?.let {
            createTestScenarioRunner(TestScenarioWrapper(feature.filePath + ":$BEFORE_FEATURE_NAME", BEFORE_FEATURE_NAME, it, null), scope)
        }
    }

    /**
     * The runners of the test scenarios of the feature which the [tagsFilter] includes.
     */
    protected val testScenarios: List<TestScenarioRunner> by lazy {
        feature.scenarios
            ?.filter { tagsFilter.checkTagsAreIncluded(it.tags) }
            ?.map { createTestScenarioRunner(it, scopeOf(it)) }
            ?: emptyList()
    }

    /**
     * Runs the steps after the feature, if it has any, in the [scope] of the feature.
     */
    protected val afterFeatureSteps: TestScenarioRunner? by lazy {
        feature.afterSteps?.let {
            createTestScenarioRunner(TestScenarioWrapper(feature.filePath + ":$AFTER_FEATURE_NAME", AFTER_FEATURE_NAME, null, it), scope)
        }
    }

    private val testScenarioRunners: List<TestScenarioRunner> by lazy {
        listOfNotNull(beforeFeatureSteps) + testScenarios + listOfNotNull(afterFeatureSteps)
    }

    override fun getDescription(): Description {
        if (description == null) {
            description = Description.createSuiteDescription(name, getId())
            children.forEach { description!!.addChild(describeChild(it)) }
        }
        return description ?: error("Failed to create description of feature: " + testEntity.getEntityName())
    }

    override fun getChildren(): List<TestScenarioRunner> = testScenarioRunners

    private fun createTestScenarioRunner(testScenario: TestScenario, scope: TestScenarioScope): TestScenarioRunner =
        TestScenarioRunner.create(testScenario, testEntity.getId(), testStepRunner, hookRunner, testStepLogger, eventDispatcher, scope)

    /**
     * The scope the steps of the [scenario] run in: a new one of its own if the feature creates them, otherwise the one
     * of the feature.
     */
    protected open fun scopeOf(scenario: TestScenario): TestScenarioScope =
        scenarioScopeFactory?.invoke("${testEntity.getEntityName()}/${scenario.name}#${Integer.toHexString(scenario.getId())}") ?: scope

    override fun describeChild(child: TestScenarioRunner): Description {
        return child.description
    }

    override fun run(notifier: RunNotifier) {
        eventDispatcher.dispatch(FeatureStartedEvent(testEntity as Feature))
        super.run(notifier)
    }

    override fun onRunCompleted() {
        try {
            if (scenarioScopeFactory != null) scope.parallelGroup.close()
        } finally {
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
