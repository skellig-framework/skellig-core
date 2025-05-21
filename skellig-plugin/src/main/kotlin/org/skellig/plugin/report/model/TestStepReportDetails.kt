package org.skellig.plugin.report.model

import org.apache.commons.lang3.time.DurationFormatUtils
import org.skellig.feature.event.TestExecutionStatus

open class TestStepReportDetails(
    val name: String,
    val parameters: Map<String, Any?>?,
    var testStepInfo: Map<String, Any?>?,
    var result: Any?,
    var executionStatus: TestExecutionStatus,
    var errorLog: String?,
    var logRecords: List<String>?,
    var duration: Long
) {

    constructor(name: String, parameters: Map<String, Any?>?) : this(name, parameters, null, null, TestExecutionStatus.RUNNING, null, null, 0)

    fun isPassed(): Boolean {
        return executionStatus == TestExecutionStatus.PASSED
    }

    fun isFailed(): Boolean {
        return executionStatus == TestExecutionStatus.FAILED
    }

    fun isIgnored(): Boolean {
        return executionStatus == TestExecutionStatus.IGNORED
    }

     fun getDurationFormatted(): String {
        return getFormattedDuration(duration)
    }

    fun getParameters(): String {
        return parameters?.map { "${it.key} = ${it.value}" }?.joinToString("\n") ?: ""
    }
}

fun getFormattedDuration(duration: Long): String {
    return if (duration > 60000) {
        DurationFormatUtils.formatDuration(duration, "mm.ss.SSS") + " min."
    } else if (duration >= 1000) {
        DurationFormatUtils.formatDuration(duration, "ss.SSS") + " sec."
    } else DurationFormatUtils.formatDuration(duration, "SSS") + " ms."
}