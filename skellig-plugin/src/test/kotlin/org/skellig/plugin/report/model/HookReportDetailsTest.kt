package org.skellig.plugin.report.model

import org.apache.commons.lang3.time.DurationFormatUtils
import org.junit.Assert
import org.junit.jupiter.api.Test
import org.skellig.feature.event.Result

class HookReportDetailsTest {

    @Test
    fun testHookReportDetailsBuilder() {
        val methodName = "testMethod"
        val errorLog = "testError"
        val logRecords = listOf("log1", "log2")
        val duration = 1000L

        val hookReportDetails = HookReportDetails(methodName, Result(duration, RuntimeException(errorLog), null), logRecords)

        Assert.assertEquals(methodName, hookReportDetails.methodName)
        Assert.assertEquals(true, hookReportDetails.result.errorLog?.startsWith("java.lang.RuntimeException: testError"))
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
        val hookReportDetails = HookReportDetails(null, Result(1000L, RuntimeException("error"), null), null)
        Assert.assertFalse(hookReportDetails.isPassed())
    }

    @Test
    fun testGetDurationFormatted() {
        val duration = 1000L
        val expectedFormat = DurationFormatUtils.formatDuration(duration, "ss.SSS") + " sec."
        val hookReportDetails = HookReportDetails(null, Result(duration, RuntimeException("error"), null), null)

        Assert.assertEquals(expectedFormat, hookReportDetails.getDurationFormatted())
    }
}