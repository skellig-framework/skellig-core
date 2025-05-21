package org.skellig.plugin.report

import org.apache.log4j.spi.LocationInfo
import org.apache.log4j.spi.LoggingEvent
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import org.mockito.Mockito
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.time.LocalDateTime
import java.util.TimeZone

class CustomAppenderTest {

    @Test
    fun testLog() {
        val testStepLogger = Mockito.mock<TestStepLogger>()
        val event = Mockito.mock<LoggingEvent>()

        whenever(event.getTimeStamp()).thenReturn(System.currentTimeMillis())
        whenever(event.message).thenReturn("text")
        whenever(event.locationInformation).thenReturn(LocationInfo("file", "classname", "method", "line"))
        val eventTime =
            LocalDateTime.ofInstant(Instant.ofEpochMilli(event.timeStamp), TimeZone.getDefault().toZoneId())

        CustomAppender(testStepLogger).doAppend(event)

        verify(testStepLogger).log(
            "${eventTime.format(CustomAppender.DATE_TIME_FORMAT)} ${event.getLevel()} " +
                    "${event.locationInformation.fullInfo} - ${event.message}"
        )
    }

    @Test
    fun testClose() {
        val testStepLogger = Mockito.mock<TestStepLogger>()
        CustomAppender(testStepLogger).close()

        verify(testStepLogger).getLogsAndClean()
    }

    @Test
    fun testRequiresLayout() {
        // false by default
        assertFalse(CustomAppender(Mockito.mock()).requiresLayout())
    }
}