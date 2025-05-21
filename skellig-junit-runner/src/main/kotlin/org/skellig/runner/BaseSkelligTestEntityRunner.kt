package org.skellig.runner

import org.junit.internal.runners.model.EachTestNotifier
import org.junit.runner.Description
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier
import org.junit.runner.notification.StoppedByUserException
import org.junit.runners.ParentRunner
import org.skellig.feature.SkelligTestEntity
import org.skellig.feature.event.*
import org.skellig.feature.hook.SkelligHookRunner
import org.skellig.plugin.report.TestStepLogger
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.util.PropertyFormatUtils
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.runner.TestStepRunner

abstract class BaseSkelligTestEntityRunner<T : SkelligTestEntity>(
    protected val testEntity: SkelligTestEntity,
    protected val hookRunner: SkelligHookRunner,
    protected val testStepRunner: TestStepRunner?,
    protected val testStepLogger: TestStepLogger,
    protected val beforeHookType: Class<out Annotation>,
    protected val afterHookType: Class<out Annotation>,
    protected val eventDispatcher: SkelligTestEventDispatcher
) : ParentRunner<T>(testEntity::class.java), SkelligTestEntity {

    protected val log = logger<BaseSkelligTestEntityRunner<T>>()
    protected var testStepRunResults = mutableListOf<TestStepProcessor.TestStepRunResult>()
    private var childDescriptions = mutableMapOf<Int, Description>()
    private var isTestFailed = false

    override fun getId(): Int = testEntity.getId()

    override fun getEntityName(): String = testEntity.getEntityName()

    override fun getEntityTags(): Set<String>? = testEntity.getEntityTags()

    override fun getName(): String = getEntityName()

    override fun run(notifier: RunNotifier) {
        val testNotifier = EachTestNotifier(
            notifier,
            description
        )
        testNotifier.fireTestSuiteStarted()
        try {
            runBeforeHooks(notifier)
            classBlock(notifier).evaluate()
            runAfterHooks(notifier)
        } catch (e: StoppedByUserException) {
            throw e
        } catch (e: Throwable) {
            testNotifier.addFailure(e)
        } finally {
            awaitForTestStepRunResults(testStepRunResults, notifier)
            testNotifier.fireTestSuiteFinished()
        }
    }

    open fun runBeforeHooks(notifier: RunNotifier) {
        runHooks(beforeHookType)
    }

    open fun runAfterHooks(notifier: RunNotifier) {
        runHooks(afterHookType)
    }

    private fun runHooks(
        hookType: Class<out Annotation>
    ) {
        hookRunner.run(testEntity.getEntityTags(), hookType) { hookName, e, duration ->
            dispatchHookFinishedEvent(hookName, duration, e, hookType)
            if (e != null) {
                throw e
            }
        }
    }

    protected abstract fun dispatchHookFinishedEvent(hookName: String, duration: Long, e: Throwable?, hookType: Class<out Annotation>)

    protected fun runTestStep(
        child: TestStepWrapper,
        childDescription: Description,
        notifier: RunNotifier
    ): TestStepProcessor.TestStepRunResult? {

        eventDispatcher.dispatch(TestStepStartedEvent(child.testStep, child.parentFeatureId, child.parentTestScenarioId, child.testStep.parameters, child.executionSequenceType))
        val testStepFinishedEvent = TestStepFinishedEvent(child.testStep.getId(), child.parentFeatureId, child.parentTestScenarioId, child.executionSequenceType)

        var runResult: TestStepProcessor.TestStepRunResult? = null
        if (isTestFailed) {
            notifier.fireTestIgnored(childDescription)
            eventDispatcher.dispatch(testStepFinishedEvent)
        } else {
            notifier.fireTestStarted(childDescription)
            testStepLogger.clear()
            val startTime = System.currentTimeMillis()
            try {
                val parameters = child.testStep.parameters ?: emptyMap()
                runResult = testStepRunner!!.run(child.testStep.name, parameters)

                // subscribe for result from test step. Usually needed for async test step
                // however if it's sync, then the function will be called anyway.
                runResult.subscribe { t, r, e ->

                    eventDispatcher.dispatch(
                        TestStepProcessingFinishedEvent(
                            child.testStep.getId(),
                            child.parentFeatureId,
                            child.parentTestScenarioId,
                            Result(System.currentTimeMillis() - startTime, e, PropertyFormatUtils.toString(r, 0)),
                            t?.getFullInfo()?: emptyMap(),
                            child.executionSequenceType
                        )
                    )

                    if (e != null) {
                        /*
                          if test step is sync, then the thrown exception will be caught in runChild
                          if test step is async, then throwing exception doesn't give any effect, however
                          it will fail on 'run' method while waiting for the result and the error will be
                          registered in the report
                        */
                        throw e
                    }
                }
            } catch (e: StoppedByUserException) {
                throw e
            } catch (e: Throwable) {
                fireFailureEvent(notifier, childDescription, e)
                testStepFinishedEvent.executionStatus = TestExecutionStatus.FAILED
            } finally {
                testStepFinishedEvent.executionStatus = TestExecutionStatus.PASSED
                testStepFinishedEvent.logRecords = testStepLogger.getLogsAndClean()
                runResult?.let { testStepRunResults.add(it) }

                notifier.fireTestFinished(childDescription)
                eventDispatcher.dispatch(testStepFinishedEvent)
            }
        }
        return runResult
    }

    protected fun describeTestStep(step: TestStepWrapper): Description {
        val id = step.testStep.getId()
        return childDescriptions.computeIfAbsent(id) { Description.createTestDescription(name, step.testStep.name, id) }
    }

    protected fun awaitForTestStepRunResults(testStepRunResults: MutableList<TestStepProcessor.TestStepRunResult>, notifier: RunNotifier) {
        try {
            log.info("Waiting for the unfinished processing of test steps if any remaining...")
            // if there are any async test step running, then wait until they're finished
            // within set timeout. Cleanup results as they are no longer needed.
            testStepRunResults.forEach {
                try {
                    it.awaitResult()
                } catch (ex: Exception) {
                    fireFailureEvent(notifier, description, ex)
                }
            }
        } finally {
            testStepRunResults.clear()
        }
    }

    private fun getStackTrace(e: Throwable): String = e.stackTraceToString()

    private fun fireFailureEvent(notifier: RunNotifier, childDescription: Description, e: Throwable) {
        notifier.fireTestFailure(Failure(childDescription, e))
        isTestFailed = true
    }

    override fun toString(): String {
        return name
    }
}