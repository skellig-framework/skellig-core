package org.skellig.runner.junit.report.model

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.skellig.runner.junit.report.attachment.ReportAttachment

class TestScenarioReportDetailsTest {

    @Test
    fun testTotalPassedTests() {
        val testStepReportDetails = TestStepReportDetails<Any>("t1", null, null, null, emptyList())

        assertEquals(1, TestScenarioReportDetails("n1", listOf(testStepReportDetails)).getTotalPassedTestSteps())
    }

    @Test
    fun testTotalFailedTestsWhenNoReportDetails() {
        assertEquals(0, TestScenarioReportDetails("n1", emptyList()).getTotalPassedTestSteps())
    }

    @Test
    fun testTotalFailedTests() {
        val testStepReportDetails = TestStepReportDetails<Any>("t1", null, null, "error", emptyList())

        val testScenarioReportDetails = TestScenarioReportDetails("n1", listOf(testStepReportDetails))
        assertEquals(0, testScenarioReportDetails.getTotalPassedTestSteps())
        assertEquals(1, testScenarioReportDetails.getTotalFailedTestSteps())
    }
}