package org.skellig.runner.junit.report.attachment.log

internal interface TestStepLogger {

    /**
     * Add a log record
     */
    fun log(text: String)

    /**
     * Get all log records in a new list and clear the current one
     */
    fun getLogsAndClean(): List<String>

}

internal class DefaultTestStepLogger : TestStepLogger {

    private var logRecords = mutableListOf<String>()

    override fun log(text: String) {
        logRecords.add(text)
    }

    override fun getLogsAndClean(): List<String> {
        val copy = logRecords.toList()
        logRecords = mutableListOf()
        return copy
    }

}