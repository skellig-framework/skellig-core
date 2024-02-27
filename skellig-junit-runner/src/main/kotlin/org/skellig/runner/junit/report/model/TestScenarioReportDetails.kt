package org.skellig.runner.junit.report.model

class TestScenarioReportDetails(
    val name: String?,
    val tags: Set<String>?,
    val beforeHooksReportDetails: List<HookReportDetails>?,
    val afterHooksReportDetails: List<HookReportDetails>?,
    val testStepReportDetails: List<TestStepReportDetails<*>>?
) {

    fun getTotalPassedTestSteps(): Int {
        return testStepReportDetails?.count { it.isPassed() } ?: 0
    }

    fun isPassed(): Boolean {
        return testStepReportDetails?.any { it.isPassed() } ?: true
    }

    fun getTagsLine(): String? {
        return tags?.joinToString(", ") { "@$it" }
    }

    fun getTotalTestSteps(): Int {
        return testStepReportDetails?.size ?: 0
    }

    fun getTotalFailedTestSteps(): Int {
        return (testStepReportDetails?.size ?: 0) - getTotalPassedTestSteps()
    }

    fun getScenarioDuration(): Long {
        return testStepReportDetails?.sumOf { it.duration } ?: 0
    }

    fun getScenarioDurationFormatted(): String {
        return getFormattedDuration(testStepReportDetails?.sumOf { it.duration } ?: 0)
    }

    fun getTotalPassedPercentage(): Float {
        return if (testStepReportDetails?.isNotEmpty() == true)
            getTotalPassedTestSteps().toFloat() / testStepReportDetails.size * 100
        else 0f
    }
}