package org.skellig.runner.junit.report.model

import org.apache.commons.lang3.time.DurationFormatUtils
import org.junit.Assert
import org.junit.Test
import org.skellig.feature.event.Result

class HookReportDetailsTest {

    @Test
    fun testHookReportDetailsBuilder() {
        val methodName = "testMethod"
        val errorLog = "testError"
        val logRecords = listOf("log1", "log2")
        val duration = 1000L

        val hookReportDetails = HookReportDetails(methodName, Result(0, null, null), logRecords)

        Assert.assertEquals(methodName, hookReportDetails.methodName)
        Assert.assertEquals(errorLog, hookReportDetails.result.errorLog)
        Assert.assertEquals(logRecords, hookReportDetails.logRecords)
        Assert.assertEquals(duration, hookReportDetails.result.duration)
    }

    @Test
    fun testIsPassedWhenErrorLogIsNull() {
        val hookReportDetails = HookReportDetails(null, Result(1000L, null, null), null)
        Assert.assertTrue(hookReportDetails.isPassed())
    }

    @Test
    fun testIsPassedWhenErrorLogIsEmpty() {
        val hookReportDetails = HookReportDetails(null, Result(1000L, null, null), null)
        Assert.assertTrue(hookReportDetails.isPassed())
    }

    @Test
    fun testIsPassedWhenErrorLogIsNotEmpty() {
        val hookReportDetails = HookReportDetails(null, Result(1000L, null, "error"), null)
        Assert.assertFalse(hookReportDetails.isPassed())
    }

    @Test
    fun testGetDurationFormatted() {
        val duration = 1000L
        val expectedFormat = DurationFormatUtils.formatDuration(duration, "ss.SSS") + " sec."
        val hookReportDetails = HookReportDetails(null, Result(duration, null, "error"), null)

        Assert.assertEquals(expectedFormat, hookReportDetails.getDurationFormatted())
    }
}