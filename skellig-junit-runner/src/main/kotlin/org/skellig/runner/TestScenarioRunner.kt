package org.skellig.runner

import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import org.skellig.feature.TestScenario
import org.skellig.feature.TestStep
import org.skellig.feature.hook.SkelligHookRunner
import org.skellig.feature.hook.annotation.AfterTestScenario
import org.skellig.feature.hook.annotation.BeforeTestScenario
import org.skellig.runner.junit.report.TestStepLogger
import org.skellig.runner.junit.report.model.TestScenarioReportDetails
import org.skellig.runner.junit.report.model.TestStepReportDetails
import org.skellig.teststep.processing.processor.TestStepProcessor.TestStepRunResult
import org.skellig.teststep.runner.TestStepRunner


open class TestScenarioRunner protected constructor(
    testScenario: TestScenario,
    testStepRunner: TestStepRunner?,
    hookRunner: SkelligHookRunner,
    testStepLogger: TestStepLogger
) : BaseSkelligTestEntityRunner<TestStep>(
    testScenario, hookRunner, testStepRunner, testStepLogger,
    BeforeTestScenario::class.java, AfterTestScenario::class.java,
    testScenario.beforeSteps, testScenario.afterSteps
) {

    companion object {
        fun create(
            testScenario: TestScenario, testStepRunner: TestStepRunner?,
            hookRunner: SkelligHookRunner, testStepLogger: TestStepLogger
        ): TestScenarioRunner {
            return TestScenarioRunner(testScenario, testStepRunner, hookRunner, testStepLogger)
        }
    }

    private var description: Description? = null
    protected var testStepRunResults: MutableList<TestStepRunResult>? = mutableListOf()
    protected var testStepsDataReport = mutableListOf<TestStepReportDetails.Builder>()

    override fun getChildren(): List<TestStep>? {
        return (testEntity as TestScenario).steps
    }

    override fun getDescription(): Description {
        if (description == null) {
            description = Description.createSuiteDescription(name, name)
            children?.forEach { step: TestStep -> description?.addChild(describeChild(step)) }
        }
        return description ?: error("Failed to create description of test scenario: " + testEntity.getEntityName())
    }

    override fun describeChild(step: TestStep): Description {
        return describeTestStep(step)
    }

    override fun runChild(child: TestStep, notifier: RunNotifier) {
        runTestStep(child, describeChild(child), notifier, testStepsDataReport)?.let {
            testStepRunResults?.add(it)
        }
    }

    override fun runAfterHooks(notifier: RunNotifier) {
        super.runAfterHooks(notifier)
        awaitForTestStepRunResults(testStepRunResults, notifier)
    }

    fun getTestScenarioReportDetails(): TestScenarioReportDetails {
        return TestScenarioReportDetails(
            name, getEntityTags(),
            beforeHookReportDetails,
            afterHookReportDetails,
            beforeTestStepsDataReport.map { it.build() }.toList(),
            afterTestStepsDataReport.map { it.build() }.toList(),
            testStepsDataReport.map { it.build() }.toList()
        )
    }

}