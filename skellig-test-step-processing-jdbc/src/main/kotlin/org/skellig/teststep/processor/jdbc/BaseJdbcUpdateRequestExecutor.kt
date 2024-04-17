package org.skellig.teststep.processor.jdbc

import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processor.db.model.DatabaseRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException

/**
 * BaseJdbcUpdateRequestExecutor is an abstract class that provides the common implementation for executing JDBC update requests.
 *
 * @param connection The JDBC Connection object.
 */
internal abstract class BaseJdbcUpdateRequestExecutor(private val connection: Connection) : BaseJdbcRequestExecutor() {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(BaseJdbcUpdateRequestExecutor::class.java)
    }

    override fun execute(databaseRequest: DatabaseRequest): Any? {
        val result: Int
        try {
            val query: String?
            result = if (databaseRequest.query != null) {
                query = databaseRequest.query
                executeUpdate(query!!,
                              databaseRequest.queryParameters
                                  ?.map { getParameterValue(it) } ?: emptyList())
            } else {
                query = composeQuery(databaseRequest, databaseRequest.columnValuePairs)
                executeUpdate(query, databaseRequest.columnValuePairs ?: emptyMap())
            }
            connection.commit()
        } catch (ex: Exception) {
            try {
                connection.rollback()
            } catch (e: SQLException) {
                LOGGER.error("Could not rollback transaction", e)
            }
            throw TestStepProcessingException(ex.message, ex)
        }
        return result
    }

    private fun executeUpdate(query: String, searchCriteria: Map<String, Any?>): Int =
        connection.prepareStatement(query).use { preparedStatement ->
            executeUpdate(preparedStatement, query, convertToRawParameters(searchCriteria))
        }

    private fun executeUpdate(query: String, queryParameters: List<Any?>): Int =
        connection.prepareStatement(query).use { preparedStatement ->
            executeUpdate(preparedStatement, query, queryParameters)
        }

    private fun executeUpdate(preparedStatement: PreparedStatement,
                              query: String,
                              parameters: List<Any?>): Int {
        for (i in parameters.indices) {
            preparedStatement.setObject(i + 1, parameters[i])
        }
        val response = preparedStatement.executeUpdate()
        LOGGER.debug("Query has been executed successfully: $query " +
                             "with parameters: $parameters")
        return response
    }

    protected abstract fun composeQuery(request: DatabaseRequest, columnValuePairs: Map<String, Any?>?): String

    protected open fun convertToRawParameters(columnValuePairs: Map<String, Any?>): List<Any?> {
        return columnValuePairs.values
            .map { getParameterValue(it) }
            .toList()
    }
}