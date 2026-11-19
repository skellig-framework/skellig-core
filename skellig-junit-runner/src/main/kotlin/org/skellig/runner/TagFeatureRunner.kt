package org.skellig.runner

import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier
import org.junit.runner.notification.StoppedByUserException
import org.junit.runners.model.Statement
import org.skellig.feature.Feature
import org.skellig.feature.TestScenario
import org.skellig.feature.event.ExecutionSequenceType
import org.skellig.feature.event.HookFinishedEvent
import org.skellig.feature.event.Result
import org.skellig.feature.event.SkelligTestEventDispatcher
import org.skellig.feature.hook.SkelligHookRunner
import org.skellig.feature.hook.annotation.AfterTestFeature
import org.skellig.feature.hook.annotation.BeforeTestFeature
import org.skellig.feature.metadata.TagsFilter
import org.skellig.plugin.report.TestStepLogger
import org.skellig.runner.parallel.ParallelGroupScheduler
import org.skellig.teststep.processing.state.ParallelGroupContext
import org.skellig.teststep.processing.state.TestScenarioScope
import org.skellig.teststep.processing.util.ScopeLogContext
import org.skellig.teststep.runner.TestStepRunner
import java.util.concurrent.CompletableFuture

/**
 * Allocates each scenario to its tag's worker without waiting. Feature hooks and step blocks bracket the
 * scenarios of this feature on each participating queue. Scenario hooks remain owned by TestScenarioRunner.
 */
internal class TagFeatureRunner(
    private val feature: Feature,
    tagsFilter: TagsFilter,
    hookRunner: SkelligHookRunner,
    testStepRunner: TestStepRunner,
    testStepLogger: TestStepLogger,
    eventDispatcher: SkelligTestEventDispatcher,
    scope: TestScenarioScope,
    private val createScope: (String, ParallelGroupContext) -> TestScenarioScope,
    private val scheduler: ParallelGroupScheduler
) : BaseFeatureRunner(feature, tagsFilter, hookRunner, testStepRunner, testStepLogger, eventDispatcher, scope, { scope }) {
    private val work = mutableListOf<CompletableFuture<Unit>>()
    private var nextScenarioIndex = 0

    // Only tag metadata is collected here. Scenarios themselves are submitted individually by runChild.
    private val queues by lazy {
        val tagsByQueue = linkedMapOf<String, MutableSet<String>>()
        feature.scenarios.orEmpty().filter { tagsFilter.checkTagsAreIncluded(it.tags) }.forEach { scenario ->
            tagsByQueue.getOrPut(scheduler.tagFor(scenario.tags)) { linkedSetOf() }.addAll(scenario.tags.orEmpty())
        }
        tagsByQueue.mapValues { (tag, tags) ->
            val featureScope = createScope("${scope.id}/$tag/feature", scheduler.groupFor(tag))
            FeatureQueue(
                featureScope, tags,
                feature.beforeSteps?.let {
                    featureSteps(TestScenarioWrapper("${feature.filePath}:$tag:before", "Before Feature", it, null), featureScope)
                },
                feature.afterSteps?.let {
                    featureSteps(TestScenarioWrapper("${feature.filePath}:$tag:after", "After Feature", null, it), featureScope)
                }
            )
        }
    }

    override fun scopeOf(scenario: TestScenario) =
        createScope("${scope.id}/${nextScenarioIndex++}/${scenario.path}", scheduler.groupFor(scheduler.tagFor(scenario.tags)))

    override fun getChildren(): List<TestScenarioRunner> =
        queues.values.mapNotNull { it.before } + testScenarios + queues.values.mapNotNull { it.after }

    // Feature hooks are queued with their matching scenarios, rather than executed on the allocation thread.
    override fun runBeforeHooks(notifier: RunNotifier) = Unit
    override fun runAfterHooks(notifier: RunNotifier) = Unit

    override fun childrenInvoker(notifier: RunNotifier): Statement = object : Statement() {
        override fun evaluate() {
            testScenarios.forEach { runChild(it, notifier) }
            queues.forEach { (tag, queue) ->
                work.add(scheduler.schedule(tag) {
                    try {
                        if (queue.setupSucceeded) {
                            queue.after?.run(notifier)
                            runFeatureHooks(queue, AfterTestFeature::class.java)
                        }
                    } catch (e: StoppedByUserException) {
                        throw e
                    } catch (e: Throwable) {
                        notifier.fireTestFailure(Failure(description, e))
                    } finally {
                        queue.scope.state.clean()
                    }
                })
            }
        }
    }

    override fun runChild(child: TestScenarioRunner, notifier: RunNotifier) {
        val tag = scheduler.tagFor(child.getEntityTags())
        val queue = queues.getValue(tag)
        if (!queue.submitted) {
            queue.submitted = true
            work.add(scheduler.schedule(tag) {
                try {
                    runFeatureHooks(queue, BeforeTestFeature::class.java)
                    queue.before?.run(notifier)
                    queue.setupSucceeded = true
                } catch (e: StoppedByUserException) {
                    throw e
                } catch (e: Throwable) {
                    notifier.fireTestFailure(Failure(description, e))
                }
            })
        }
        work.add(scheduler.schedule(tag) {
            try {
                if (queue.setupSucceeded) child.run(notifier)
            } finally {
                child.scenarioScope.state.clean()
            }
        })
    }

    override fun afterChildren(completion: () -> Unit) = scheduler.afterAll(work, completion)

    private fun runFeatureHooks(queue: FeatureQueue, type: Class<out Annotation>) {
        ScopeLogContext.runWith(queue.scope) {
            hookRunner.run(queue.tags, type, queue.scope) { name, error, duration ->
                eventDispatcher.dispatch(HookFinishedEvent(
                    name, feature.getId(), null, testStepLogger.getLogsAndClean(queue.scope.id), Result(duration, error, null),
                    if (type == BeforeTestFeature::class.java) ExecutionSequenceType.BEFORE else ExecutionSequenceType.AFTER
                ))
                if (error != null) throw error
            }
        }
    }

    private fun featureSteps(scenario: TestScenario, featureScope: TestScenarioScope) = TestScenarioRunner.create(
        scenario, feature.getId(), testStepRunner, NO_SCENARIO_HOOKS, testStepLogger, eventDispatcher, featureScope
    )

    private class FeatureQueue(
        val scope: TestScenarioScope,
        val tags: Set<String>,
        val before: TestScenarioRunner?,
        val after: TestScenarioRunner?,
        var submitted: Boolean = false,
        var setupSucceeded: Boolean = false
    )

    companion object {
        // Feature step blocks are not scenarios and must not trigger scenario hook methods.
        private val NO_SCENARIO_HOOKS = object : SkelligHookRunner {
            override fun run(tags: Set<String>?, hookType: Class<out Annotation>, scope: TestScenarioScope,
                             onRunCompleted: (String, Throwable?, Long) -> Unit) = Unit
        }
    }
}
