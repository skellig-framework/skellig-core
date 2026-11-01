package org.skellig.plugin.report

import org.apache.log4j.AppenderSkeleton
import org.apache.log4j.spi.LoggingEvent
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*



/**
 * Appends log events to a [TestStepLogger] which are used in report generation.
 *
 * A log event belongs to the scope whose id its message starts with, in the form `[<scope id>]: <text>`.
 * The log events without it belong to no scope.
 *
 * @see [org.skellig.runner.junit.report.ReportGenerator]
 *
 * @property testStepLogger The TestStepLogger instance to which log events are appended
 */
class CustomAppender(private val testStepLogger: TestStepLogger) : AppenderSkeleton() {

    companion object {
        internal val DATE_TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSS")

        private val SCOPE_PREFIX = Regex("^\\[(.+?)]: ")
    }

    override fun append(event: LoggingEvent) {
        val eventTime =
            LocalDateTime.ofInstant(Instant.ofEpochMilli(event.timeStamp), TimeZone.getDefault().toZoneId())
        val message = "${event.message}"

        testStepLogger.log(
            SCOPE_PREFIX.find(message)?.groupValues?.get(1),
            "${eventTime.format(DATE_TIME_FORMAT)} ${event.getLevel()} ${event.locationInformation.fullInfo} - $message"
        )
    }

    override fun close() {
        testStepLogger.clear()
    }

    override fun requiresLayout(): Boolean {
        return false
    }
}
