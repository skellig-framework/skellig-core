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
    val beforeHooksReportDetails: MutableList<HookReportDetails> = mutableListOf(),
    val afterHooksReportDetails: MutableList<HookReportDetails> = mutableListOf(),
    val beforeReportDetails: MutableMap<Any, org.skellig.runner.plugin.TestStepReportDetails> = mutableMapOf(),
    val afterReportDetails: MutableMap<Any, org.skellig.runner.plugin.TestStepReportDetails> = mutableMapOf(),
    val testStepReportDetails: MutableMap<Any, org.skellig.runner.plugin.TestStepReportDetails> = mutableMapOf()
) {

    fun getTotalPassedTestSteps(): Int {
        return testStepReportDetails?.count { it.value.isPassed() } ?: 0
    }

    fun isPassed(): Boolean {
        return testStepReportDetails?.any { it.value.isPassed() } ?: true
    }

    fun getTagsLine(): String? {
        return tags?.joinToString(" ")
    }

    fun getTotalTestSteps(): Int {
        return testStepReportDetails.size
    }

    fun getTotalFailedTestSteps(): Int {
        return testStepReportDetails.size - getTotalPassedTestSteps()
    }

    fun getScenarioDuration(): Long {
        return testStepReportDetails.values.sumOf { it.duration } ?: 0
    }

    fun getScenarioDurationFormatted(): String {
        return getFormattedDuration(testStepReportDetails.values.sumOf { it.duration } ?: 0)
    }

    fun getBeforeScenarioDurationFormatted(): String {
        return getFormattedDuration(
            (beforeReportDetails.values.sumOf { it.duration } ?: 0)
        )
    }

    fun getAfterScenarioDurationFormatted(): String {
        return getFormattedDuration(
            (afterReportDetails.values.sumOf { it.duration } ?: 0)
        )
    }

    fun getBeforeHooksDurationFormatted(): String {
        return getFormattedDuration(
            (beforeHooksReportDetails.sumOf { it.result.duration } ?: 0)
        )
    }

    fun getAfterHooksDurationFormatted(): String {
        return getFormattedDuration(
            (afterHooksReportDetails.sumOf { it.result.duration } ?: 0)
        )
    }

    fun getTotalPassedPercentage(): Float {
        return if (testStepReportDetails.isNotEmpty())
            getTotalPassedTestSteps().toFloat() / testStepReportDetails.size * 100
        else 0f
    }
}