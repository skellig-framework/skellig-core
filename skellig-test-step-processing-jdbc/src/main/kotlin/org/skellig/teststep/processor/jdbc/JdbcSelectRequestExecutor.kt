package org.skellig.teststep.processor.jdbc

import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processor.db.model.DatabaseRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.*

/**
 * This class is responsible for executing JDBC select queries on the database.
 *
 * @property connection The JDBC connection used to connect to the database.
 */
internal class JdbcSelectRequestExecutor(private val connection: Connection?) : BaseJdbcRequestExecutor() {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(JdbcSelectRequestExecutor::class.java)

        private const val COMPARATOR = "comparator"
        private const val DEFAULT_VALUE_PLACEHOLDER = "?"
        private const val DEFAULT_COMPARATOR = "="
    }

    override fun execute(databaseRequest: DatabaseRequest): Any {
        return try {
            val query: String?
            if (databaseRequest.query != null) {
                query = databaseRequest.query
                executeQuery(query!!,
                             databaseRequest.queryParameters
                                 ?.map { getParameterValue(it) } ?: emptyList())
            } else {
                val searchCriteria = databaseRequest.columnValuePairs ?: emptyMap()
                query = composeFindQuery(databaseRequest, searchCriteria)
                executeQuery(query, searchCriteria)
            }
        } catch (ex: Exception) {
            throw TestStepProcessingException(ex.message, ex)
        }
    }

    private fun executeQuery(query: String, searchCriteria: Map<String, Any?>): Any =
        connection!!.prepareStatement(query).use { preparedStatement ->
            executeQuery(preparedStatement, query, convertToRawParameters(searchCriteria))
        }

    private fun executeQuery(query: String, queryParameters: List<Any?>): Any =
        connection!!.prepareStatement(query).use { preparedStatement ->
            executeQuery(preparedStatement, query, queryParameters)
        }

    private fun executeQuery(preparedStatement: PreparedStatement,
                             query: String,
                             queryParameters: List<Any?>): List<Map<String, Any?>> {
        for (i in queryParameters.indices) {
            preparedStatement.setObject(i + 1, queryParameters[i])
        }
        val response = executeQuery(preparedStatement)

        LOGGER.debug("Query has been executed successfully: $query " +
                             "with parameters: $queryParameters " +
                             "and response: $response")
        return response
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

    private fun convertToRawParameters(searchCriteria: Map<String, Any?>): List<Any?> {
        return searchCriteria.values
            .map { getParameterValue(it) }
            .toList()
    }

    private fun composeFindQuery(databaseRequest: DatabaseRequest, searchCriteria: Map<String, Any?>): String {
        val queryBuilder = StringBuilder()
        queryBuilder.append("SELECT * FROM ")
        queryBuilder.append(databaseRequest.table)
        if (searchCriteria.isNotEmpty()) {
            queryBuilder.append(" WHERE ")
            val columns = searchCriteria.entries.joinToString(separator = " AND ") {
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