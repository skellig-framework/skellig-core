package org.skellig.plugin.report

import org.apache.log4j.AppenderSkeleton
import org.apache.log4j.spi.LoggingEvent
import org.skellig.teststep.processing.util.ScopeLogContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*



/**
 * Appends log events to a [TestStepLogger] which are used in report generation.
 *
 * A log event belongs to the scope whose id is in the log context (MDC) of the thread that logged it, under
 * [ScopeLogContext.KEY], whatever its message is. The log events logged by a thread which was not given a scope
 * belong to no scope.
 *
 * @see [org.skellig.runner.junit.report.ReportGenerator]
 *
 * @property testStepLogger The TestStepLogger instance to which log events are appended
 */
class CustomAppender(private val testStepLogger: TestStepLogger) : AppenderSkeleton() {

    companion object {
        internal val DATE_TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSS")
    }

    override fun append(event: LoggingEvent) {
        val eventTime =
            LocalDateTime.ofInstant(Instant.ofEpochMilli(event.timeStamp), TimeZone.getDefault().toZoneId())

        testStepLogger.log(
            event.getMDC(ScopeLogContext.KEY)?.toString(),
            "${eventTime.format(DATE_TIME_FORMAT)} ${event.getLevel()} ${event.locationInformation.fullInfo} - ${event.message}"
        )
    }

    override fun close() {
        testStepLogger.clear()
    }

    override fun requiresLayout(): Boolean {
        return false
    }
}
