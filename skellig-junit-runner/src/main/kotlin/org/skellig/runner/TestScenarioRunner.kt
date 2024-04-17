package org.skellig.runner

import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import org.skellig.feature.TestScenario
import org.skellig.feature.hook.SkelligHookRunner
import org.skellig.feature.hook.annotation.AfterTestScenario
import org.skellig.feature.hook.annotation.BeforeTestScenario
import org.skellig.runner.junit.report.TestStepLogger
import org.skellig.runner.junit.report.model.TestScenarioReportDetails
import org.skellig.runner.junit.report.model.TestStepReportDetails
import org.skellig.teststep.runner.TestStepRunner


/**
 * The TestScenarioRunner class is responsible for running [test scenarios][TestScenarioRunner].
 *
 * @param testScenario The test scenario to be executed.
 * @param testStepRunner The test step runner used to execute individual test steps.
 * @param hookRunner The hook runner used to execute before and after hooks.
 * @param testStepLogger The test step logger used to log test step execution details.
 */
open class TestScenarioRunner protected constructor(
    testScenario: TestScenario,
    testStepRunner: TestStepRunner?,
    hookRunner: SkelligHookRunner,
    testStepLogger: TestStepLogger
) : BaseSkelligTestEntityRunner<TestStepWrapper>(
    testScenario, hookRunner, testStepRunner, testStepLogger,
    BeforeTestScenario::class.java, AfterTestScenario::class.java
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
    protected var testStepsDataReport = mutableListOf<TestStepReportDetails.Builder>()

    override fun getChildren(): List<TestStepWrapper>? {
        val testScenario = testEntity as TestScenario
        var testSteps: List<TestStepWrapper>? = null
        testScenario.beforeSteps
            ?.map { TestStepWrapper(it, TestStepRunnerType.BEFORE) }
            ?.let { testSteps = it }
        testScenario.steps
            ?.map { TestStepWrapper(it) }
            ?.let { testSteps = testSteps?.plus(it) ?: it }
        testScenario.afterSteps
            ?.map { TestStepWrapper(it, TestStepRunnerType.AFTER) }
            ?.let { testSteps = testSteps?.plus(it) ?: it }
        return testSteps
    }

    override fun getDescription(): Description {
        if (description == null) {
            description = Description.createSuiteDescription(name, getId())
            children?.forEach { step -> description?.addChild(describeChild(step)) }
        }
        return description ?: error("Failed to create description of test scenario: " + testEntity.getEntityName())
    }

    override fun describeChild(step: TestStepWrapper): Description {
        return describeTestStep(step)
    }

    override fun runChild(child: TestStepWrapper, notifier: RunNotifier) {
        val report = when (child.type) {
            TestStepRunnerType.BEFORE -> beforeTestStepsDataReport
            TestStepRunnerType.AFTER -> afterTestStepsDataReport
            else -> testStepsDataReport
        }
        runTestStep(child, describeChild(child), notifier, report)
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