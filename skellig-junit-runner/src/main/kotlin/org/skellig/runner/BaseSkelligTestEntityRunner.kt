package org.skellig.runner

import org.junit.runner.Description
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier
import org.junit.runners.ParentRunner
import org.skellig.feature.SkelligTestEntity
import org.skellig.feature.TestStep
import org.skellig.feature.hook.SkelligHookRunner
import org.skellig.runner.junit.report.TestStepLogger
import org.skellig.runner.junit.report.model.HookReportDetails
import org.skellig.runner.junit.report.model.TestStepReportDetails
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.runner.TestStepRunner
import java.io.PrintWriter
import java.io.StringWriter

abstract class BaseSkelligTestEntityRunner<T : SkelligTestEntity>(
    protected val testEntity: SkelligTestEntity,
    protected val hookRunner: SkelligHookRunner,
    protected val testStepRunner: TestStepRunner?,
    protected val testStepLogger: TestStepLogger,
    protected val beforeHookType: Class<out Annotation>,
    protected val afterHookType: Class<out Annotation>,
    protected val beforeSteps: List<TestStep>?,
    protected val afterSteps: List<TestStep>?
) : ParentRunner<T>(testEntity::class.java), SkelligTestEntity {

    protected val beforeHookReportDetails = mutableListOf<HookReportDetails>()
    protected val afterHookReportDetails = mutableListOf<HookReportDetails>()
    protected var beforeTestStepsDataReport = mutableListOf<TestStepReportDetails.Builder>()
    protected var afterTestStepsDataReport = mutableListOf<TestStepReportDetails.Builder>()
    private var testStepHooksRunResults: MutableList<TestStepProcessor.TestStepRunResult>? = mutableListOf()
    private var isTestFailed = false

    override fun getEntityName(): String = testEntity.getEntityName()

    override fun getEntityTags(): Set<String>? = testEntity.getEntityTags()

    override fun getName(): String = getEntityName()

    override fun run(notifier: RunNotifier) {
        try {
            runBeforeHooks(notifier)
            super.run(notifier)
        } finally {
            runAfterHooks(notifier)
        }
    }

    protected open fun runBeforeHooks(notifier: RunNotifier) {
        runTestSteps(beforeSteps, notifier, beforeTestStepsDataReport)
            ?.filterNotNull()
            ?.forEach { result -> testStepHooksRunResults?.add(result) }
        hookRunner.run(testEntity.getEntityTags(), beforeHookType) { name, e, duration ->
            beforeHookReportDetails.add(createHookReportDetails(name, e, duration))
        }
    }

    protected open fun runAfterHooks(notifier: RunNotifier) {
        runTestSteps(afterSteps, notifier, afterTestStepsDataReport)
            ?.filterNotNull()
            ?.forEach { result -> testStepHooksRunResults?.add(result) }
        hookRunner.run(testEntity.getEntityTags(), afterHookType) { name, e, duration ->
            afterHookReportDetails.add(createHookReportDetails(name, e, duration))
        }
        awaitForTestStepRunResults(testStepHooksRunResults, notifier)
    }

    protected fun runTestStep(
        child: TestStep,
        childDescription: Description,
        notifier: RunNotifier,
        testStepsDataReport: MutableList<TestStepReportDetails.Builder>
    ): TestStepProcessor.TestStepRunResult? {

        val testStepReportBuilder =
            TestStepReportDetails.Builder()
                .withName(child.name)
                .withParameters(child.parameters)
        var runResult: TestStepProcessor.TestStepRunResult? = null
        if (isTestFailed) {
            notifier.fireTestIgnored(childDescription)
            testStepsDataReport.add(testStepReportBuilder)
        } else {
            notifier.fireTestStarted(childDescription)
            testStepLogger.clear()
            try {
                val startTime = System.currentTimeMillis()
                val parameters = child.parameters ?: emptyMap()
                runResult = testStepRunner!!.run(child.name, parameters)

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
                testStepReportBuilder.withOriginalTestStep(child.name).withErrorLog(attachStackTrace(e))
                fireFailureEvent(notifier, childDescription, e)
            } finally {
                testStepsDataReport.add(testStepReportBuilder.withLogRecords(testStepLogger.getLogsAndClean()))
                notifier.fireTestFinished(childDescription)
            }
        }
        return runResult
    }

    private fun runTestSteps(
        testSteps: List<TestStep>?,
        notifier: RunNotifier,
        testStepsDataReport: MutableList<TestStepReportDetails.Builder>
    ): List<TestStepProcessor.TestStepRunResult?>? {
        return testSteps
            ?.map { testStep ->
                runTestStep(testStep, describeTestStep(testStep), notifier, testStepsDataReport)
            }?.toList()
    }

    protected fun describeTestStep(step: TestStep): Description {
        return Description.createTestDescription(name, step.name, step.name)
    }

    private fun createHookReportDetails(name: String, e: Throwable?, duration: Long) =
        HookReportDetails(name, e?.message, testStepLogger.getLogsAndClean(), duration)

    protected fun awaitForTestStepRunResults(testStepRunResults: MutableList<TestStepProcessor.TestStepRunResult>?, notifier: RunNotifier) {
        try {
            // if there are any async test step running, then wait until they're finished
            // within set timeout. Cleanup results as they are no longer needed.
            testStepRunResults?.forEach {
                try {
                    it.awaitResult()
                } catch (ex: Exception) {
                    fireFailureEvent(notifier, description, ex)
                }
            }
        } finally {
            testStepRunResults?.clear()
        }
    }

    private fun attachStackTrace(e: Throwable): String = e.stackTraceToString()

    private fun fireFailureEvent(notifier: RunNotifier, childDescription: Description, e: Throwable) {
        notifier.fireTestFailure(Failure(childDescription, e))
        isTestFailed = true
    }
}