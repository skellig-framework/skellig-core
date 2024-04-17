package org.skellig.teststep.processor.jdbc

import org.skellig.teststep.processor.db.model.DatabaseRequest
import java.sql.Connection

/**
 * JdbcInsertRequestExecutor is a class that handles the execution of JDBC insert requests.
 *
 * @param connection The JDBC connection object.
 */
internal class JdbcInsertRequestExecutor(connection: Connection)
    : BaseJdbcUpdateRequestExecutor(connection) {

    override fun composeQuery(request: DatabaseRequest, columnValuePairs: Map<String, Any?>?): String {
        val columnValuePairsToInsert =
                columnValuePairs ?: error("Cannot insert empty data to table " + request.table)
        val queryBuilder = StringBuilder()
        queryBuilder.append(request.command)
        queryBuilder.append(" INTO ")
        queryBuilder.append(request.table)
        queryBuilder.append(" (")
        appendColumns(columnValuePairsToInsert, queryBuilder)
        queryBuilder.append(") VALUES(")
        appendValues(columnValuePairsToInsert, queryBuilder)
        queryBuilder.append(")")
        return queryBuilder.toString()
    }

    private fun appendColumns(columnValuePairs: Map<String, Any?>, queryBuilder: StringBuilder) {
        queryBuilder.append(columnValuePairs.keys.joinToString(","))
    }

    private fun appendValues(columnValuePairs: Map<String, Any?>, queryBuilder: StringBuilder) {
        queryBuilder.append(columnValuePairs.values.joinToString(",") { "?" })
    }
}