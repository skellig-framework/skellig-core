package org.skellig.teststep.processor.jdbc

import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processor.db.model.DatabaseRequest
import java.sql.Connection
import java.sql.SQLException

internal class JdbcInsertRequestExecutor(private val connection: Connection?) : BaseJdbcRequestExecutor() {

    override fun execute(databaseRequest: DatabaseRequest): Any? {
        var result: Int
        try {
            val query: String?
            if (databaseRequest.query != null) {
                query = databaseRequest.query
                result = connection!!.createStatement().executeUpdate(query)
            } else {
                val insertData: Map<String, Any?> = databaseRequest.columnValuePairs
                        ?: throw TestStepProcessingException("Cannot insert empty data to table " + databaseRequest.table)
                query = composeInsertQuery(databaseRequest, insertData)

                connection!!.prepareStatement(query).use { preparedStatement ->
                    val rawParameters = convertToRawParameters(insertData)
                    for (i in rawParameters.indices) {
                        preparedStatement.setObject(i + 1, rawParameters[i])
                    }
                    result = preparedStatement.executeUpdate()
                }
            }
            connection.commit()
        } catch (ex: Exception) {
            try {
                connection!!.rollback()
            } catch (e: SQLException) {
                //log later
            }
            throw TestStepProcessingException(ex.message, ex)
        }
        return result
    }

    private fun composeInsertQuery(request: DatabaseRequest?, columnValuePairs: Map<String, Any?>): String {
        val queryBuilder = StringBuilder()
        queryBuilder.append(request!!.command)
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
        queryBuilder.append(
                columnValuePairs.values.joinToString(",") { "?" })
    }
}