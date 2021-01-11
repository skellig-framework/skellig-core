package org.skellig.runner.junit.report.model

class TestScenarioReportDetails(val name: String?,
                                val testStepReportDetails: List<TestStepReportDetails>?) {

    fun getTotalPassedTestSteps(): Int {
        return testStepReportDetails
                ?.filter { obj: TestStepReportDetails -> obj.isPassed() }
                ?.count() ?: 0
    }

    fun isPassed(): Boolean {
        return testStepReportDetails?.any { it.isPassed() } ?: true
    }

    fun getTotalFailedTestSteps(): Int {
        return testStepReportDetails?.size ?: 0 - getTotalPassedTestSteps()
    }
}