package org.skellig.plugin.report.model

import org.skellig.feature.event.TestExecutionStatus
import org.skellig.feature.event.Result


/**
 * Represents report details for hooks run before or after test scenario or feature.
 *
 * @property methodName The name of the method.
 * @property errorLog The error log.
 * @property logRecords The list of log records.
 * @property duration The duration of the hook.
 */
class HookReportDetails(
    val methodName: String?,
    val result: Result,
    val logRecords: List<String>?
) {

    fun isPassed(): Boolean {
        return result.executionStatus == TestExecutionStatus.PASSED
    }

    fun isFailed(): Boolean {
        return result.executionStatus == TestExecutionStatus.FAILED
    }

    fun isIgnored(): Boolean {
        return result.executionStatus == TestExecutionStatus.IGNORED
    }

    fun getDurationFormatted(): String {
        return getFormattedDuration(result.duration)
    }
}