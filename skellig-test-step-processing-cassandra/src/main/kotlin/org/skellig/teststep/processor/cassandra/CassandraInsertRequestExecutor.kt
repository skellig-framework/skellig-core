package org.skellig.teststep.processor.cassandra

import com.datastax.driver.core.Session
import com.datastax.driver.core.SimpleStatement
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processor.db.model.DatabaseRequest

internal class CassandraInsertRequestExecutor(private val session: Session) : BaseCassandraRequestExecutor() {

    override fun execute(databaseRequest: DatabaseRequest): Any? {
        return try {
            databaseRequest.query?.let {
                return session.execute(databaseRequest.query)
            } ?: run {
                val searchCriteria = databaseRequest.columnValuePairs
                        ?: throw TestStepProcessingException("Cannot insert empty data to table " + databaseRequest.table)
                val query = composeInsertQuery(databaseRequest, searchCriteria)
                val rawParameters = convertToRawParameters(searchCriteria)
                val preparedStatement = SimpleStatement(query, *rawParameters)

                return session.execute(preparedStatement)
            }
        } catch (ex: Exception) {
            throw TestStepProcessingException(ex.message, ex)
        }
    }

    private fun composeInsertQuery(request: DatabaseRequest, columnValuePairs: Map<String, Any?>): String {
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

    private fun appendColumns(columnValuePairs: Map<String, Any?>, queryBuilder: StringBuilder) {
        queryBuilder.append(java.lang.String.join(",", columnValuePairs.keys))
    }

    private fun convertToRawParameters(columnValuePairs: Map<String, Any?>): Array<Any?> {
        return columnValuePairs.values
                .map { getParameterValue(it) }
                .toTypedArray()
    }

    private fun appendValues(columnValuePairs: Map<String, Any?>, queryBuilder: StringBuilder) {
        queryBuilder.append(columnValuePairs.values
                .joinToString(",") { "?" })
    }
}