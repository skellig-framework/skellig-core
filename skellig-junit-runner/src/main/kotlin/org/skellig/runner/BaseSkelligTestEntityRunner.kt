package org.skellig.runner

import org.junit.internal.AssumptionViolatedException
import org.junit.internal.runners.model.EachTestNotifier
import org.junit.runner.Description
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier
import org.junit.runner.notification.StoppedByUserException
import org.junit.runners.ParentRunner
import org.skellig.feature.SkelligTestEntity
import org.skellig.feature.hook.SkelligHookRunner
import org.skellig.runner.junit.report.TestStepLogger
import org.skellig.runner.junit.report.model.HookReportDetails
import org.skellig.runner.junit.report.model.TestStepReportDetails
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.runner.TestStepRunner

abstract class BaseSkelligTestEntityRunner<T : SkelligTestEntity>(
    protected val testEntity: SkelligTestEntity,
    protected val hookRunner: SkelligHookRunner,
    protected val testStepRunner: TestStepRunner?,
    protected val testStepLogger: TestStepLogger,
    protected val beforeHookType: Class<out Annotation>,
    protected val afterHookType: Class<out Annotation>
) : ParentRunner<T>(testEntity::class.java), SkelligTestEntity {

    protected val beforeHookReportDetails = mutableListOf<HookReportDetails>()
    protected val afterHookReportDetails = mutableListOf<HookReportDetails>()
    protected var beforeTestStepsDataReport = mutableListOf<TestStepReportDetails.Builder>()
    protected var afterTestStepsDataReport = mutableListOf<TestStepReportDetails.Builder>()
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
        } catch (e: AssumptionViolatedException) {
            testNotifier.addFailedAssumption(e)
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
        runHooks(beforeHookType, beforeHookReportDetails)
    }

    open fun runAfterHooks(notifier: RunNotifier) {
        runHooks(afterHookType, afterHookReportDetails)
    }

    private fun runHooks(
        hookType: Class<out Annotation>,
        hookReportDetails: MutableList<HookReportDetails>
    ) {
        hookRunner.run(testEntity.getEntityTags(), hookType) { hookName, e, duration ->
            hookReportDetails.add(createHookReportDetails(hookName, e, duration))
            if (e != null) {
                throw e
            }
        }
    }

    protected fun runTestStep(
        child: TestStepWrapper,
        childDescription: Description,
        notifier: RunNotifier,
        testStepsDataReport: MutableList<TestStepReportDetails.Builder>
    ): TestStepProcessor.TestStepRunResult? {

        val testStepReportBuilder =
            TestStepReportDetails.Builder()
                .withName(child.testStep.name)
                .withParameters(child.testStep.parameters)
        var runResult: TestStepProcessor.TestStepRunResult? = null
        if (isTestFailed) {
            notifier.fireTestIgnored(childDescription)
            testStepsDataReport.add(testStepReportBuilder)
        } else {
            notifier.fireTestStarted(childDescription)
            testStepLogger.clear()
            try {
                val startTime = System.currentTimeMillis()
                val parameters = child.testStep.parameters ?: emptyMap()
                runResult = testStepRunner!!.run(child.testStep.name, parameters)

                // subscribe for result from test step. Usually needed for async test step
                // however if it's sync, then the function will be called anyway.

                // subscribe for result from test step. Usually needed for async test step
                // however if it's sync, then the function will be called anyway.
                runResult.subscribe { t, r, e ->
                    testStepReportBuilder.withOriginalTestStep(t)
                        .withResult(r)
                        .withDuration((System.currentTimeMillis() - startTime))
                    if (e != null) {
                        /*
                          if test step is sync, then the thrown exception will be caught in runChild
                          if test step is async, then throwing exception doesn't give any effect, however
                          it will fail on 'run' method while waiting for the result and the error will be
                          registered in the report
                        */
                        testStepReportBuilder.withErrorLog(attachStackTrace(e))
                        throw e
                    }
                }
            } catch (e: Throwable) {
                testStepReportBuilder.withOriginalTestStep(child.testStep.name).withErrorLog(attachStackTrace(e))
                fireFailureEvent(notifier, childDescription, e)
            } finally {
                testStepsDataReport.add(testStepReportBuilder.withLogRecords(testStepLogger.getLogsAndClean()))
                runResult?.let { testStepRunResults.add(it) }
                notifier.fireTestFinished(childDescription)
            }
        }
        return runResult
    }

    protected fun describeTestStep(step: TestStepWrapper): Description {
        val id = step.testStep.getId()
        return childDescriptions.computeIfAbsent(id) { Description.createTestDescription(name, step.testStep.name, id) }
    }

    private fun createHookReportDetails(name: String, e: Throwable?, duration: Long) =
        HookReportDetails(name, e?.message, testStepLogger.getLogsAndClean(), duration)

    protected fun awaitForTestStepRunResults(testStepRunResults: MutableList<TestStepProcessor.TestStepRunResult>, notifier: RunNotifier) {
        try {
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

    private fun attachStackTrace(e: Throwable): String = e.stackTraceToString()

    private fun fireFailureEvent(notifier: RunNotifier, childDescription: Description, e: Throwable) {
        notifier.fireTestFailure(Failure(childDescription, e))
        isTestFailed = true
    }
}