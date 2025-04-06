package org.skellig.runner.junit.report.model

/**
 * Represents the details of a feature report which includes a list of [TestScenarioReportDetails]
 *
 * @property name The name of the feature.
 * @property tags The set of tags associated with the feature.
 * @property beforeHooksReportDetails The list of before hooks report details.
 * @property afterHooksReportDetails The list of after hooks report details.
 * @property beforeReportDetails The list of before report details.
 * @property afterReportDetails The list of after report details.
 * @property testScenarioReportDetails The list of test scenario report details.
 */
class FeatureReportDetails(
    val name: String?,
    val tags: Set<String>?,
    val beforeHooksReportDetails: MutableList<HookReportDetails> = mutableListOf(),
    val afterHooksReportDetails: MutableList<HookReportDetails> = mutableListOf(),
    val beforeReportDetails: MutableMap<Any, org.skellig.runner.plugin.TestStepReportDetails> = mutableMapOf(),
    val afterReportDetails: MutableMap<Any, org.skellig.runner.plugin.TestStepReportDetails> = mutableMapOf(),
    val testScenarioReportDetails: MutableMap<Any, TestScenarioReportDetails> = mutableMapOf(),
) {

    private var totalTestSteps = -1
    private var totalPassedTestSteps = -1

    fun getTotalTestSteps(): Int {
        if (totalTestSteps == -1) {
            totalTestSteps = testScenarioReportDetails
                .map { item -> item.value.testStepReportDetails.size }
                .reduce { a, b -> Integer.sum(a, b) } ?: 0
        }
        return totalTestSteps
    }

    fun getTotalPassedTestSteps(): Int {
        if (totalPassedTestSteps == -1) {
            totalPassedTestSteps = testScenarioReportDetails
                .map { obj -> obj.value.getTotalPassedTestSteps() }
                .reduce { acc, a -> Integer.sum(a, acc) } ?: 0
        }
        return totalPassedTestSteps
    }

    fun getHooksReportDetails(): List<HookReportDetails> {
        return beforeHooksReportDetails.plus(afterHooksReportDetails)
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

    fun getBeforeFeatureDurationFormatted(): String {
        return getFormattedDuration(
            beforeReportDetails.values.sumOf { it.duration }
        )
    }

    fun getAfterFeatureDurationFormatted(): String {
        return getFormattedDuration(
            afterReportDetails.values.sumOf { it.duration }
        )
    }

    private fun getTotalHooksDuration() =
        getHooksReportDetails().sumOf { it.result.duration }

    fun getTotalDuration(): String {
        return getFormattedDuration(
            testScenarioReportDetails.values.sumOf { it.getScenarioDuration() } +
                    getTotalHooksDuration())
    }

    fun isPassed(): Boolean {
        return testScenarioReportDetails.any { it.value.isPassed() }
    }

    fun getTotalPassedPercentage(): Float {
        return getTotalPassedTestSteps().toFloat() / getTotalTestSteps() * 100
    }

    fun getTagsLine(): String? {
        return tags?.joinToString(" ")
    }
}