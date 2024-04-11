package org.skellig.teststep.processor.cassandra

import com.datastax.oss.driver.api.core.CqlSession
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processor.db.model.DatabaseRequest

internal class CassandraUpdateRequestExecutor(session: CqlSession,
                                              private val insertExecutor: CassandraInsertRequestExecutor,
                                              private val selectExecutor: CassandraSelectRequestExecutor)
    : BaseCassandraUpdateRequestExecutor(session) {

    companion object {
        private const val DEFAULT_VALUE_PLACEHOLDER = "?"
        private const val WHERE = "where"
    }

    override fun execute(databaseRequest: DatabaseRequest): Any? {
        if (databaseRequest.query == null) {
            val where = getWhereCriteria(databaseRequest)
            val selectRequest = DatabaseRequest("select", databaseRequest.table, where)
            val selectResult = selectExecutor.execute(selectRequest) as List<*>
            if (selectResult.isEmpty()) {
                val insertRequest = DatabaseRequest("insert", databaseRequest.table,
                                                   extractAllColumnValuePairs(databaseRequest.columnValuePairs, where))
                return insertExecutor.execute(insertRequest)
            }
        }
        return super.execute(databaseRequest)
    }

    override fun composeQuery(request: DatabaseRequest, columnValuePairs: Map<String, Any?>): String {
        val queryBuilder = StringBuilder()
        queryBuilder.append("UPDATE ")
        queryBuilder.append(request.table)
        queryBuilder.append(" SET ")
        queryBuilder.append(
            columnValuePairs.entries
                .filter { it.key != WHERE }
                .joinToString(",") { it.key + "=" + DEFAULT_VALUE_PLACEHOLDER })
        queryBuilder.append(" WHERE ")
        queryBuilder.append(
            getWhereCriteria(request)
                .map { "${it.key}=$DEFAULT_VALUE_PLACEHOLDER" }
                .joinToString(","))
        return queryBuilder.toString()
    }

    override fun convertToRawParameters(columnValuePairs: Map<String, Any?>): Array<Any?> {
        return extractAllColumnValuePairs(columnValuePairs, columnValuePairs[WHERE] as Map<String, Any?>).values
            .map { getParameterValue(it) }
            .toTypedArray()
    }

    private fun extractAllColumnValuePairs(columnValuePairs: Map<String, Any?>?,
                                           where: Map<String, Any?>): MutableMap<String, Any?> {
        val columnValuePairsToInsert = columnValuePairs!!
            .filter { it.key != WHERE }
            .toMutableMap()
        columnValuePairsToInsert.putAll(where)
        return columnValuePairsToInsert
    }

    private fun getWhereCriteria(databaseRequest: DatabaseRequest): Map<String, Any?> =
        (databaseRequest.columnValuePairs
            ?: error("Cannot update empty data in table " + databaseRequest.table))[WHERE] as Map<String, Any?>?
            ?: error("Update operation for table ${databaseRequest.table} " +
                             "must have 'where' clause to understand which records to update")
}