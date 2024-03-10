package org.skellig.runner.junit.report.model

import org.skellig.feature.TestStep

class FeatureReportDetails(val name: String?,
                           val tags: Set<String>?,
                           val beforeHooksReportDetails: List<HookReportDetails>?,
                           val afterHooksReportDetails: List<HookReportDetails>?,
                           val beforeReportDetails: List<TestStepReportDetails<*>>?,
                           val afterReportDetails: List<TestStepReportDetails<*>>?,
                           val testScenarioReportDetails: List<TestScenarioReportDetails>?) {

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

    fun getTotalDuration(): String {
        return getFormattedDuration(testScenarioReportDetails?.sumOf { it.getScenarioDuration() } ?: 0)
    }

    fun isPassed(): Boolean {
        return testScenarioReportDetails?.any { it.isPassed() } == true
    }

    fun getTotalPassedPercentage(): Float {
        return getTotalPassedTestSteps().toFloat() / getTotalTestSteps() * 100
    }

    fun getTagsLine(): String? {
        return tags?.joinToString(", ") { "@$it" }
    }
}