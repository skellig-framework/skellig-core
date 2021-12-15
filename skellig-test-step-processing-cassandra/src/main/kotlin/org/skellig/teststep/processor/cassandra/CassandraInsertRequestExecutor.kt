package org.skellig.teststep.processor.cassandra

import com.datastax.oss.driver.api.core.CqlSession
import org.skellig.teststep.processor.db.model.DatabaseRequest

internal open class CassandraInsertRequestExecutor(session: CqlSession) : BaseCassandraUpdateRequestExecutor(session) {

    override fun composeQuery(request: DatabaseRequest, columnValuePairs: Map<String, Any?>): String {
        val queryBuilder = StringBuilder()
        queryBuilder.append(request.command)
        queryBuilder.append(" INTO ")
        queryBuilder.append(request.table)
        queryBuilder.append(" (")
        appendColumns(columnValuePairs, queryBuilder)
        queryBuilder.append(") VALUES(")
        appendValues(columnValuePairs, queryBuilder)
        queryBuilder.append(")")

        return queryBuilder.toString()
    }

    override fun convertToRawParameters(columnValuePairs: Map<String, Any?>): Array<Any?> {
        return columnValuePairs.values
            .map { getParameterValue(it) }
            .toTypedArray()
    }

    private fun appendColumns(columnValuePairs: Map<String, Any?>, queryBuilder: StringBuilder) {
        queryBuilder.append(java.lang.String.join(",", columnValuePairs.keys))
    }

    private fun appendValues(columnValuePairs: Map<String, Any?>, queryBuilder: StringBuilder) {
        queryBuilder.append(columnValuePairs.values
                                .joinToString(",") { "?" })
    }
}