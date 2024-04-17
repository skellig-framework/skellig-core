package org.skellig.teststep.processor.cassandra

import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.ResultSet
import com.datastax.oss.driver.api.core.cql.Row
import com.datastax.oss.driver.api.core.cql.SimpleStatement
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.util.debug
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processor.db.model.DatabaseRequest
import java.util.function.Consumer

/**
 * This class is responsible for executing Cassandra select queries on the database using.
 *
 * @property session The Cassandra session used to execute queries in the database.
 */
internal open class CassandraSelectRequestExecutor(private val session: CqlSession) : BaseCassandraRequestExecutor() {

    companion object {
        private const val COMPARATOR = "comparator"
    }

    private val log = logger<CassandraSelectRequestExecutor>()

    override fun execute(databaseRequest: DatabaseRequest): Any {
        return try {
            if (databaseRequest.query != null) {
                executeQuery(databaseRequest.query!!,
                    databaseRequest.queryParameters
                        ?.map { getParameterValue(it) }
                        ?.toTypedArray() ?: emptyArray())
            } else {
                val searchCriteria = databaseRequest.columnValuePairs ?: emptyMap()
                val query = composeFindQuery(databaseRequest, searchCriteria)
                executeQuery(query, convertToRawParameters(searchCriteria))
            }
        } catch (ex: Exception) {
            throw TestStepProcessingException(ex.message, ex)
        }
    }

    private fun executeQuery(query: String, queryParameters: Array<Any?>): Any {
        val response = extractFromResultSet(session.execute(SimpleStatement.newInstance(query, *queryParameters)))
        log.debug {
            "Query has been executed successfully: $query " +
                    "with parameters: ${queryParameters.contentToString()} " +
                    "and response: $response"
        }
        return response
    }

    private fun extractFromResultSet(resultSet: ResultSet): List<Map<String, Any?>> {
        val result: MutableList<Map<String, Any?>> = ArrayList()
        resultSet.forEach(Consumer { row: Row ->
            val resultRow: MutableMap<String, Any?> = LinkedHashMap()
            result.add(resultRow)
            row.columnDefinitions
                .forEach(Consumer { column ->
                    resultRow[column.name.asInternal()] = row.getObject(column.name)
                })
        })
        return result
    }

    private fun convertToRawParameters(searchCriteria: Map<String, Any?>): Array<Any?> {
        return searchCriteria.values.stream()
            .map { getParameterValue(it) }
            .toArray()
    }

    private fun composeFindQuery(databaseRequest: DatabaseRequest, searchCriteria: Map<String, Any?>): String {
        val queryBuilder = StringBuilder()
        queryBuilder.append("SELECT * FROM ")
        queryBuilder.append(databaseRequest.table)
        if (searchCriteria.isNotEmpty()) {
            queryBuilder.append(" WHERE ")
            val columns = searchCriteria.entries
                .joinToString(" AND ") {
                    if (it.value is Map<*, *>) {
                        val comparator = (it.value as Map<*, *>)[COMPARATOR].toString()
                        it.key + comparator + " ?"
                    } else {
                        it.key + getCompareOperator(it.value) + "?"
                    }
                }
            queryBuilder.append(columns)
        }
        return queryBuilder.append(" ALLOW FILTERING").toString()
    }

    private fun getCompareOperator(valueToCompare: Any?): String {
        return if (valueToCompare.toString().contains("%")) " like " else " = "
    }
}