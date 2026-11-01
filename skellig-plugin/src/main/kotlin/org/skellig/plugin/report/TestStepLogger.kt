package org.skellig.plugin.report

import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

/**
 * The TestStepLogger interface provides methods for logging and retrieving log records during test step executions.
 * Each scenario scope collects its own log records, identified by the id of the scope, so scenarios which run
 * at the same time do not see the log records of each other.
 * These logs are used by [org.skellig.runner.junit.report.ReportGenerator] to display logs captured per each Test Step run.
 */
interface TestStepLogger {

    /**
     * Add a log record to the scope [scopeId]. The log record is dropped if it belongs to no scope that collects it.
     *
     * @param scopeId The id of the scope the log record was logged for, or null if it was logged outside any scope.
     */
    fun log(scopeId: String?, text: String)

    /**
     * Get all log records of the scope in a new list and clear them
     */
    fun getLogsAndClean(scopeId: String): List<String>

    /**
     * Clear all log records of the scope. The scope then starts to collect its log records.
     */
    fun clear(scopeId: String)

    /**
     * Clear all log records of all scopes
     */
    fun clear()
}

/**
 * Keeps a buffer of log records per scope. A scope starts to collect its log records when it is cleared for the first time.
 *
 * A log record without a scope, or for a scope that has not started to collect, goes to the scope [unscopedRecordsScopeId]
 * if there is one, otherwise it is dropped. Without parallel scenarios there is only one scope, which then collects all
 * the log records, including the ones of no scope. With parallel scenarios it must not be set, as a log record
 * of no scope can not be attributed to one scenario.
 *
 * @property unscopedRecordsScopeId The id of the scope that collects the log records which belong to no scope.
 */
class DefaultTestStepLogger(private val unscopedRecordsScopeId: String? = null) : TestStepLogger {

    private val logRecords = ConcurrentHashMap<String, MutableList<String>>()

    override fun log(scopeId: String?, text: String) {
        bufferFor(scopeId)?.add(text)
    }

    override fun getLogsAndClean(scopeId: String): List<String> {
        val scopeLogRecords = logRecords[scopeId] ?: return emptyList()
        // the lock is the list itself, which is also what the list uses to guard its own operations
        synchronized(scopeLogRecords) {
            val copy = scopeLogRecords.toList()
            scopeLogRecords.clear()
            return copy
        }
    }

    override fun clear(scopeId: String) {
        logRecords.computeIfAbsent(scopeId) { newBuffer() }.clear()
    }

    override fun clear() {
        logRecords.values.forEach { it.clear() }
    }

    /**
     * The buffer of the scope if it has started to collect, otherwise the buffer for unscoped records, which is null
     * if there is none.
     */
    private fun bufferFor(scopeId: String?): MutableList<String>? =
        scopeId?.let { logRecords[it] }
            ?: unscopedRecordsScopeId?.let { logRecords.computeIfAbsent(it) { newBuffer() } }

    private fun newBuffer(): MutableList<String> = Collections.synchronizedList(mutableListOf())
}
