package org.skellig.plugin.report

import org.skellig.plugin.report.model.FeatureReportDetails


/**
 * Generates a test report with execution details of Feature, Test Scenario and Test Steps.
 */
interface ReportGenerator {

    /**
     * Generates a test report from [org.skellig.runner.junit.report.model.FeatureReportDetails].
     *
     * @param testReportDetails The list of FeatureReportDetails containing the details of the test report.
     */
    fun generate(testReportDetails: List<FeatureReportDetails>?)
}