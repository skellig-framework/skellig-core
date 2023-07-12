package org.skellig.runner.junit.report

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.apache.log4j.Level
import org.apache.log4j.spi.LoggingEvent
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.skellig.runner.junit.report.attachment.log.TestStepLogger
import java.time.LocalDateTime
import java.time.ZoneOffset

class CustomAppenderTest {

    private val testStepLogger = mock<TestStepLogger>()
    private val customAppender = CustomAppender(testStepLogger)

    @Test
    fun testLog() {
        val timestamp = LocalDateTime.of(2010, 10, 10, 10, 0,30, 10).toInstant(ZoneOffset.UTC).toEpochMilli()
        val loggingEvent = LoggingEvent("", mock(), timestamp, Level.INFO, "msg", mock())

        customAppender.doAppend(loggingEvent)

        verify(testStepLogger).log("10-10-2010 11:00:30.000 INFO null - msg")
    }

    @Test
    fun testClose() {
        customAppender.close()

        verify(testStepLogger).getLogsAndClean()
    }

    @Test
    fun testLayoutAlwaysFalse() {
       assertFalse(customAppender.requiresLayout())
    }
}