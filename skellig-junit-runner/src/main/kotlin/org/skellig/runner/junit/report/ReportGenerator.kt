package org.skellig.runner.junit.report

import org.skellig.runner.junit.report.model.FeatureReportDetails

interface ReportGenerator {

    fun generate(testReportDetails: List<FeatureReportDetails>?)
}