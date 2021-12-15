package org.skellig.teststep.processor.cassandra

import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.SimpleStatement
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processor.db.model.DatabaseRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal abstract class BaseCassandraUpdateRequestExecutor(private val session: CqlSession) :
    BaseCassandraRequestExecutor() {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(BaseCassandraUpdateRequestExecutor::class.java)
    }

    override fun execute(databaseRequest: DatabaseRequest): Any? {
        try {
            databaseRequest.query?.let {
                return executeUpdate(databaseRequest.query!!,
                                     databaseRequest.queryParameters
                                         ?.map { getParameterValue(it) }
                                         ?.toTypedArray() ?: emptyArray())
            } ?: run {
                val searchCriteria = databaseRequest.columnValuePairs
                    ?: throw TestStepProcessingException("Cannot insert empty data to table " + databaseRequest.table)

                return executeUpdate(composeQuery(databaseRequest, searchCriteria),
                                     convertToRawParameters(searchCriteria))
            }
        } catch (ex: Exception) {
            throw TestStepProcessingException(ex.message, ex)
        }
    }

    private fun executeUpdate(query: String, rawParameters: Array<Any?>): Any {
        val response = session.execute(SimpleStatement.newInstance(query, *rawParameters))
        LOGGER.debug("Query has been executed successfully: $query " +
                             "with parameters: ${rawParameters.contentToString()}")
        return response
    }

    protected abstract fun composeQuery(request: DatabaseRequest, columnValuePairs: Map<String, Any?>): String

    protected abstract fun convertToRawParameters(columnValuePairs: Map<String, Any?>): Array<Any?>

}