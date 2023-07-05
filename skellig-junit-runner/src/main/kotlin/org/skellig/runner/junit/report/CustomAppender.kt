package org.skellig.runner.junit.report

import org.apache.log4j.AppenderSkeleton
import org.apache.log4j.spi.LoggingEvent
import org.skellig.runner.junit.report.attachment.log.TestStepLogger
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


internal class CustomAppender(private val testStepLogger: TestStepLogger) : AppenderSkeleton() {

    companion object {
        private val DATE_TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSS")
    }

    override fun append(event: LoggingEvent) {
        val eventTime =
            LocalDateTime.ofInstant(Instant.ofEpochMilli(event.timeStamp), TimeZone.getDefault().toZoneId())

        testStepLogger.log("${eventTime.format(DATE_TIME_FORMAT)} ${event.getLevel()} " +
                "${event.locationInformation.fullInfo} - ${event.message}")
    }

    override fun close() {
        testStepLogger.getLogsAndClean()
    }

    override fun requiresLayout(): Boolean {
        return false
    }
}
