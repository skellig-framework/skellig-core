package org.skellig.plugin.report.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class FeatureReportDetailsTest {

    @Test
    fun `check if feature failed when at least one scenario is failed`() {
        val scenarioReportDetails = mock<TestScenarioReportDetails>()
        whenever(scenarioReportDetails.isFailed()).thenReturn(true)

        val featureReportDetails = FeatureReportDetails(
            name = "feature 1",
            tags = emptySet(),
            testScenarioReportDetails = mutableMapOf(Pair(0, scenarioReportDetails), Pair(1, mock<TestScenarioReportDetails>()))
        )

        assertTrue(featureReportDetails.isFailed())
    }

    @Test
    fun `check after feature duration formatted`() {
        val scenarioReportDetails1 = mock<TestStepReportDetails>()
        whenever(scenarioReportDetails1.duration).thenReturn(10)

        val scenarioReportDetails2 = mock<TestStepReportDetails>()
        whenever(scenarioReportDetails2.duration).thenReturn(5)

        val scenarioReportDetails3 = mock<TestStepReportDetails>()
        whenever(scenarioReportDetails3.duration).thenReturn(12)

        val featureReportDetails = FeatureReportDetails(
            name = "feature 1",
            tags = emptySet(),
            afterReportDetails = mutableMapOf(Pair(0, scenarioReportDetails1), Pair(1, scenarioReportDetails2)),
            beforeReportDetails = mutableMapOf(Pair(0, scenarioReportDetails2), Pair(1, scenarioReportDetails3))
        )

        assertEquals("017 ms.", featureReportDetails.getBeforeFeatureDurationFormatted())
        assertEquals("015 ms.", featureReportDetails.getAfterFeatureDurationFormatted())
    }
}