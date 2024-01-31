package org.skellig.runner

import com.typesafe.config.Config
import org.apache.log4j.BasicConfigurator
import org.junit.runner.Description
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier
import org.junit.runners.ParentRunner
import org.junit.runners.model.InitializationError
import org.skellig.feature.TestScenario
import org.skellig.feature.TestStep
import org.skellig.runner.exception.FeatureRunnerException
import org.skellig.runner.junit.report.CustomAppender
import org.skellig.runner.junit.report.DefaultTestStepLogger
import org.skellig.runner.junit.report.TestStepLogger
import org.skellig.runner.junit.report.model.TestScenarioReportDetails
import org.skellig.runner.junit.report.model.TestStepReportDetails
import org.skellig.teststep.processing.processor.TestStepProcessor.TestStepRunResult
import org.skellig.teststep.runner.TestStepRunner
import java.io.PrintWriter
import java.io.StringWriter


open class TestScenarioRunner protected constructor(
    private val testScenario: TestScenario,
    private val testStepRunner: TestStepRunner?,
    config: Config?
) : ParentRunner<TestStep>(testScenario.javaClass) {

    companion object {
        private const val REPORT_LOG_ENABLED = "report.log.enabled"
        fun create(testScenario: TestScenario, testStepRunner: TestStepRunner?, config: Config?): TestScenarioRunner {
            return try {
                TestScenarioRunner(testScenario, testStepRunner, config)
            } catch (e: InitializationError) {
                throw FeatureRunnerException(e.message, e)
            }
        }
    }

    private val testStepLogger: TestStepLogger = DefaultTestStepLogger()
    private var stepDescriptions = hashMapOf<Any, Description>()
    private var testStepsDataReport = mutableListOf<TestStepReportDetails.Builder>()
    private var testStepRunResults: MutableList<TestStepRunResult>? = mutableListOf()
    private var isChildFailed = false

    init {
        config?.let {
            if (it.hasPath(REPORT_LOG_ENABLED) && it.getBoolean(REPORT_LOG_ENABLED))
                BasicConfigurator.configure(CustomAppender(testStepLogger))
        }
    }

    override fun getChildren(): List<TestStep>? {
        return testScenario.steps
    }

    override fun getName(): String {
        return testScenario.name
    }

    override fun getDescription(): Description {
        return stepDescriptions.computeIfAbsent(this) {
            val description = Description.createSuiteDescription(name, testScenario.name)
            children?.forEach { step: TestStep -> description.addChild(describeChild(step)) }
            description
        }
    }

    override fun describeChild(step: TestStep): Description {
        return stepDescriptions.getOrDefault(step, Description.createTestDescription(name, step.name, step.name))
    }

    override fun run(notifier: RunNotifier) {
        try {
            super.run(notifier)
        } finally {
            try {
                // if there are any async test step running, then wait until they're finished
                // within set timeout. Cleanup results as they are no longer needed.
                testStepRunResults?.forEach { it.awaitResult() }
            } catch (ex: Exception) {
                fireFailureEvent(notifier, description, ex)
            } finally {
                testStepRunResults = null
            }
        }
    }

    override fun runChild(child: TestStep, notifier: RunNotifier) {
        val childDescription = describeChild(child)
        val testStepReportBuilder =
            TestStepReportDetails.Builder()
                .withName(child.name)
                .withParameters(child.parameters)
        if (isChildFailed) {
            notifier.fireTestIgnored(childDescription)
            testStepsDataReport.add(testStepReportBuilder)
        } else {
            notifier.fireTestStarted(childDescription)
            testStepLogger.clear()
            try {
                val startTime = System.currentTimeMillis()
                val parameters = child.parameters ?: emptyMap()
                val runResult = testStepRunner!!.run(child.name, parameters)
                testStepRunResults!!.add(runResult)

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
    }

    fun getTestScenarioReportDetails(): TestScenarioReportDetails {
        val testStepReportDetails = testStepsDataReport
            .map { it.build() }
            .toList()
        return TestScenarioReportDetails(name, testStepReportDetails)
    }


    private fun attachStackTrace(e: Throwable): String {
        try {
            StringWriter().use { stringWriter ->
                PrintWriter(stringWriter).use { stackTraceWriter ->
                    stackTraceWriter.append("\n")
                    e.printStackTrace(stackTraceWriter)
                    return stringWriter.toString()
                }
            }
        } catch (ioException: Exception) {
            return ""
        }
    }

    private fun fireFailureEvent(notifier: RunNotifier, childDescription: Description, e: Throwable) {
        notifier.fireTestFailure(Failure(childDescription, e))
        isChildFailed = true
    }
}