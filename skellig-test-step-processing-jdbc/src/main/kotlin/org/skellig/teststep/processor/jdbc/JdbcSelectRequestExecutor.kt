package org.skellig.teststep.processor.jdbc

import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processor.db.model.DatabaseRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.*

internal class JdbcSelectRequestExecutor(private val connection: Connection?) : BaseJdbcRequestExecutor() {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(JdbcSelectRequestExecutor::class.java)

        private const val COMPARATOR = "comparator"
        private const val DEFAULT_VALUE_PLACEHOLDER = "?"
        private const val DEFAULT_COMPARATOR = "="
    }

    override fun execute(databaseRequest: DatabaseRequest): Any {
        try {
            val query: String?
            if (databaseRequest.query != null) {
                query = databaseRequest.query
                val response = executeQuery(query!!, connection!!.createStatement())

                LOGGER.debug("Query has been executed successfully: $query and response: $response")
                return response
            } else {
                val searchCriteria = databaseRequest.columnValuePairs ?: emptyMap()
                query = composeFindQuery(databaseRequest, searchCriteria)
                connection!!.prepareStatement(query).use { preparedStatement ->
                    val rawParameters = convertToRawParameters(searchCriteria)
                    for (i in rawParameters.indices) {
                        preparedStatement.setObject(i + 1, rawParameters[i])
                    }
                    val response = executeQuery(preparedStatement)

                    LOGGER.debug("Query has been executed successfully: $query " +
                                         "with parameters: ${rawParameters.contentToString()} " +
                                         "and response: $response")
                    return response
                }
            }
        } catch (ex: Exception) {
            throw TestStepProcessingException(ex.message, ex)
        }
    }

    @Throws(SQLException::class)
    private fun executeQuery(query: String, statement: Statement): List<Map<String, Any?>> {
        return extractFromResultSet(statement.executeQuery(query))
    }

    @Throws(SQLException::class)
    private fun executeQuery(statement: PreparedStatement): List<Map<String, Any?>> {
        return extractFromResultSet(statement.executeQuery())
    }

    @Throws(SQLException::class)
    private fun extractFromResultSet(resultSet: ResultSet): List<Map<String, Any?>> {
        val result = mutableListOf<Map<String, Any?>>()
        val columns = extractColumns(resultSet)
        while (resultSet.next()) {
            val row = linkedMapOf<String, Any?>()
            for (column in columns) {
                row[column] = resultSet.getObject(column)
            }
            result.add(row)
        }
        return result
    }

    private fun convertToRawParameters(searchCriteria: Map<String, Any?>): Array<Any?> {
        return searchCriteria.values
                .map { getParameterValue(it) }
                .toTypedArray()
    }

    private fun composeFindQuery(databaseRequest: DatabaseRequest, searchCriteria: Map<String, Any?>): String {
        val queryBuilder = StringBuilder()
        queryBuilder.append("SELECT * FROM ")
        queryBuilder.append(databaseRequest.table)
        if (searchCriteria.isNotEmpty()) {
            queryBuilder.append(" WHERE ")
            val columns = searchCriteria.entries
                    .map {
                        var comparator: String? = DEFAULT_COMPARATOR
                        var valuePlaceholder = DEFAULT_VALUE_PLACEHOLDER
                        if (it.value is Map<*, *>) {
                            comparator = (it.value as Map<*, *>)[COMPARATOR] as String?
                            if ("in" == comparator) {
                                valuePlaceholder = "(?)"
                            }
                        }
                        String.format("%s %s %s", it.key, comparator, valuePlaceholder)
                    }
                    .joinToString(separator = " AND ")
            queryBuilder.append(columns)
        }
        return queryBuilder.toString()
    }

    @Throws(SQLException::class)
    private fun extractColumns(resultSet: ResultSet): List<String> {
        val metadata = resultSet.metaData
        val columns = mutableListOf<String>()

        for (i in 1..metadata.columnCount) {
            columns.add(metadata.getColumnName(i))
        }
        return columns
    }
}