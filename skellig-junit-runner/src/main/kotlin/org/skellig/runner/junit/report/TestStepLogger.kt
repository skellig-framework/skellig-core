package org.skellig.runner.junit.report

/**
 * The TestStepLogger interface provides methods for logging and retrieving log records during test step executions.
 * These logs are used by [ReportGenerator] to display logs captured per each Test Step run.
 */
interface TestStepLogger {

    /**
     * Add a log record
     */
    fun log(text: String)

    /**
     * Get all log records in a new list and clear the current pne
     */
    fun getLogsAndClean(): List<String>

    /**
     * Clear all log records
     */
    fun clear();
}

class DefaultTestStepLogger : TestStepLogger {

    private var logRecords = mutableListOf<String>()

    override fun log(text: String) {
        logRecords.add(text)
    }

    override fun getLogsAndClean(): List<String> {
        val copy = logRecords.toList()
        logRecords = mutableListOf()
        return copy
    }

    override fun clear() {
        logRecords.clear()
    }

}