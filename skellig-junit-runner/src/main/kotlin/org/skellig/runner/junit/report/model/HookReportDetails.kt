package org.skellig.runner.junit.report.model

class HookReportDetails(
    val methodName: String?,
    val errorLog: String?,
    val logRecords: List<String>?,
    val duration: Long
) {

    fun isPassed(): Boolean {
        return errorLog == null || errorLog == ""
    }

    fun getDurationFormatted(): String {
        return getFormattedDuration(duration)
    }

    class Builder {
        private var methodName: String? = null
        private var errorLog: String? = null
        private var logRecords: List<String>? = null
        private var duration: Long = 0

        fun withMethodName(methodName: String?) = apply {
            this.methodName = methodName
        }

        fun withErrorLog(errorLog: String?) = apply {
            this.errorLog = errorLog
        }

        fun withLogRecords(logRecords: List<String>?) = apply {
            this.logRecords = logRecords
        }

        fun withDuration(duration: Long) = apply {
            this.duration = duration
        }

        fun build(): HookReportDetails {
            return HookReportDetails(methodName, errorLog, logRecords, duration)
        }
    }
}