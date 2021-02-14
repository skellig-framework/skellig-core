package org.skellig.teststep.processor.cassandra

import com.datastax.driver.core.*
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processor.db.model.DatabaseRequest
import java.util.*
import java.util.function.Consumer

internal class CassandraSelectRequestExecutor(private val session: Session) : BaseCassandraRequestExecutor() {

    companion object {
        private const val COMPARATOR = "comparator"
    }

    override fun execute(databaseRequest: DatabaseRequest): Any {
        return try {
            if (databaseRequest.query != null) {
                extractFromResultSet(session.execute(databaseRequest.query))
            } else {
                val searchCriteria = databaseRequest.columnValuePairs ?: emptyMap()
                val query = composeFindQuery(databaseRequest, searchCriteria)
                val rawParameters = convertToRawParameters(searchCriteria)
                val preparedStatement: Statement = SimpleStatement(query, *rawParameters)
                extractFromResultSet(session.execute(preparedStatement))
            }
        } catch (ex: Exception) {
            throw TestStepProcessingException(ex.message, ex)
        }
    }

    private fun extractFromResultSet(resultSet: ResultSet): List<Map<String, Any?>> {
        val result: MutableList<Map<String, Any?>> = ArrayList()
        resultSet.forEach(Consumer { row: Row ->
            val resultRow: MutableMap<String, Any?> = LinkedHashMap()
            result.add(resultRow)
            row.columnDefinitions
                    .forEach(Consumer { column: ColumnDefinitions.Definition -> resultRow[column.name] = row.getObject(column.name) })
        })
        return result
    }

    private fun convertToRawParameters(searchCriteria: Map<String, Any?>): Array<Any> {
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