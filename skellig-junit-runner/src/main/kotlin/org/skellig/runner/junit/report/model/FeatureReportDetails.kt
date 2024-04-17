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
    val beforeHooksReportDetails: List<HookReportDetails>?,
    val afterHooksReportDetails: List<HookReportDetails>?,
    val beforeReportDetails: List<TestStepReportDetails<*>>?,
    val afterReportDetails: List<TestStepReportDetails<*>>?,
    val testScenarioReportDetails: List<TestScenarioReportDetails>?
) {

    private var totalTestSteps = -1
    private var totalPassedTestSteps = -1

    fun getTotalTestSteps(): Int {
        if (totalTestSteps == -1) {
            totalTestSteps = testScenarioReportDetails
                ?.map { item: TestScenarioReportDetails -> item.testStepReportDetails!!.size }
                ?.reduce { a, b -> Integer.sum(a, b) } ?: 0
        }
        return totalTestSteps
    }

    fun getTotalPassedTestSteps(): Int {
        if (totalPassedTestSteps == -1) {
            totalPassedTestSteps = testScenarioReportDetails
                ?.map { obj: TestScenarioReportDetails -> obj.getTotalPassedTestSteps() }
                ?.reduce { acc, a -> Integer.sum(a, acc) } ?: 0
        }
        return totalPassedTestSteps
    }

    fun getHooksReportDetails(): List<HookReportDetails>? {
        return beforeHooksReportDetails?.plus(afterHooksReportDetails?: emptyList())
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

    fun getBeforeFeatureDurationFormatted(): String {
        return getFormattedDuration(
            (beforeReportDetails?.sumOf { it.duration } ?: 0)
        )
    }

    fun getAfterFeatureDurationFormatted(): String {
        return getFormattedDuration(
            (afterReportDetails?.sumOf { it.duration } ?: 0)
        )
    }

    private fun getTotalHooksDuration() =
        (getHooksReportDetails()?.sumOf { it.duration } ?: 0)

    fun getTotalDuration(): String {
        return getFormattedDuration(
            (testScenarioReportDetails?.sumOf { it.getScenarioDuration() } ?: 0) +
                    getTotalHooksDuration())
    }

    fun isPassed(): Boolean {
        return testScenarioReportDetails?.any { it.isPassed() } == true
    }

    fun getTotalPassedPercentage(): Float {
        return getTotalPassedTestSteps().toFloat() / getTotalTestSteps() * 100
    }

    fun getTagsLine(): String? {
        return tags?.joinToString(" ")
    }
}