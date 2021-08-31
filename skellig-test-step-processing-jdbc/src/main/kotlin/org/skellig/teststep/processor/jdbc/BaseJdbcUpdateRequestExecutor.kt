package org.skellig.teststep.processor.jdbc

import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processor.db.model.DatabaseRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.SQLException

internal abstract class BaseJdbcUpdateRequestExecutor(private val connection: Connection?) : BaseJdbcRequestExecutor() {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(BaseJdbcUpdateRequestExecutor::class.java)
    }

    override fun execute(databaseRequest: DatabaseRequest): Any? {
        var result: Int
        try {
            val query: String?
            if (databaseRequest.query != null) {
                query = databaseRequest.query
                result = connection!!.createStatement().executeUpdate(query)

                LOGGER.debug("Query has been executed successfully: $query")
            } else {
                query = composeQuery(databaseRequest, databaseRequest.columnValuePairs)

                connection!!.prepareStatement(query).use { preparedStatement ->
                    val rawParameters = convertToRawParameters(databaseRequest.columnValuePairs?: emptyMap())
                    for (i in rawParameters.indices) {
                        preparedStatement.setObject(i + 1, rawParameters[i])
                    }
                    result = preparedStatement.executeUpdate()

                    LOGGER.debug("Query has been executed successfully: $query " +
                                         "with parameters: ${rawParameters.contentToString()}")
                }
            }
            connection.commit()
        } catch (ex: Exception) {
            try {
                connection!!.rollback()
            } catch (e: SQLException) {
                LOGGER.error("Could not rollback transaction", e)
            }
            throw TestStepProcessingException(ex.message, ex)
        }
        return result
    }

    protected abstract fun composeQuery(request: DatabaseRequest, columnValuePairs: Map<String, Any?>?): String

    protected open fun convertToRawParameters(columnValuePairs: Map<String, Any?>): Array<Any?> {
        return columnValuePairs.values
                .map { getParameterValue(it) }
                .toTypedArray()
    }
}