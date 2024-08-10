package org.skellig.runner.junit.report.model

import org.apache.commons.lang3.time.DurationFormatUtils
import org.junit.Assert
import org.junit.Test

class HookReportDetailsTest {

    @Test
    fun testHookReportDetailsBuilder() {
        val methodName = "testMethod"
        val errorLog = "testError"
        val logRecords = listOf("log1", "log2")
        val duration = 1000L

        val hookReportDetails = HookReportDetails.Builder()
            .withMethodName(methodName)
            .withErrorLog(errorLog)
            .withLogRecords(logRecords)
            .withDuration(duration)
            .build()

        Assert.assertEquals(methodName, hookReportDetails.methodName)
        Assert.assertEquals(errorLog, hookReportDetails.errorLog)
        Assert.assertEquals(logRecords, hookReportDetails.logRecords)
        Assert.assertEquals(duration, hookReportDetails.duration)
    }

    @Test
    fun testIsPassedWhenErrorLogIsNull() {
        val hookReportDetails = HookReportDetails(null, null, null, 1000L)
        Assert.assertTrue(hookReportDetails.isPassed())
    }

    @Test
    fun testIsPassedWhenErrorLogIsEmpty() {
        val hookReportDetails = HookReportDetails(null, "", null, 1000L)
        Assert.assertTrue(hookReportDetails.isPassed())
    }

    @Test
    fun testIsPassedWhenErrorLogIsNotEmpty() {
        val hookReportDetails = HookReportDetails(null, "error", null, 1000L)
        Assert.assertFalse(hookReportDetails.isPassed())
    }

    @Test
    fun testGetDurationFormatted() {
        val duration = 1000L
        val expectedFormat = DurationFormatUtils.formatDuration(duration, "ss.SSS") + " sec."
        val hookReportDetails = HookReportDetails(null, null, null, duration)

        Assert.assertEquals(expectedFormat, hookReportDetails.getDurationFormatted())
    }
}