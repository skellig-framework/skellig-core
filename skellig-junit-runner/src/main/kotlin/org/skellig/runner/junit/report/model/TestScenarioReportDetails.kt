package org.skellig.runner.junit.report.model

/**
 * Represents the report details for a test scenario.
 *
 * @property name The name of the test scenario.
 * @property tags The set of tags associated with the test scenario.
 * @property beforeHooksReportDetails The list of report details for hooks run before the test scenario.
 * @property afterHooksReportDetails The list of report details for hooks run after the test scenario.
 * @property beforeReportDetails The list of report details for steps executed before the test scenario.
 * @property afterReportDetails The list of report details for steps executed after the test scenario.
 * @property testStepReportDetails The list of report details for test steps in the test scenario.
 */
class TestScenarioReportDetails(
    val name: String?,
    val tags: Set<String>?,
    val beforeHooksReportDetails: List<HookReportDetails>?,
    val afterHooksReportDetails: List<HookReportDetails>?,
    val beforeReportDetails: List<TestStepReportDetails<*>>?,
    val afterReportDetails: List<TestStepReportDetails<*>>?,
    val testStepReportDetails: List<TestStepReportDetails<*>>?
) {

    fun getTotalPassedTestSteps(): Int {
        return testStepReportDetails?.count { it.isPassed() } ?: 0
    }

    fun isPassed(): Boolean {
        return testStepReportDetails?.any { it.isPassed() } ?: true
    }

    fun getTagsLine(): String? {
        return tags?.joinToString(" ")
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

    fun getBeforeScenarioDurationFormatted(): String {
        return getFormattedDuration(
            (beforeReportDetails?.sumOf { it.duration } ?: 0)
        )
    }

    fun getAfterScenarioDurationFormatted(): String {
        return getFormattedDuration(
            (afterReportDetails?.sumOf { it.duration } ?: 0)
        )
    }

    fun getBeforeHooksDurationFormatted(): String {
        return getFormattedDuration(
            (beforeHooksReportDetails?.sumOf { it.duration } ?: 0)
        )
    }

    fun getAfterHooksDurationFormatted(): String {
        return getFormattedDuration(
            (afterHooksReportDetails?.sumOf { it.duration } ?: 0)
        )
    }

    fun getTotalPassedPercentage(): Float {
        return if (testStepReportDetails?.isNotEmpty() == true)
            getTotalPassedTestSteps().toFloat() / testStepReportDetails.size * 100
        else 0f
    }
}