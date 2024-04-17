package org.skellig.teststep.processor.jdbc

import org.skellig.teststep.processor.db.model.DatabaseRequest
import java.sql.Connection

/**
 * JdbcUpdateRequestExecutor is a class that handles the execution of JDBC update requests.
 * When requested, it uses [JdbcSelectRequestExecutor] to check if a record exists in DB and if not, executes in insert query
 * using [JdbcInsertRequestExecutor].
 *
 * @property connection The JDBC connection object.
 * @property selectExecutor The [JdbcSelectRequestExecutor] used to execute select queries.
 * @property insertExecutor The [JdbcInsertRequestExecutor] used to execute insert queries.
 */
internal class JdbcUpdateRequestExecutor(
    connection: Connection,
    private val selectExecutor: JdbcSelectRequestExecutor,
    private val insertExecutor: JdbcInsertRequestExecutor
) : BaseJdbcUpdateRequestExecutor(connection) {

    companion object {
        private const val DEFAULT_VALUE_PLACEHOLDER = "?"
        private const val WHERE = "where"
    }

    override fun execute(databaseRequest: DatabaseRequest): Any? {
        if (databaseRequest.query == null) {
            val where = getWhereCriteria(databaseRequest)
            val selectResult = selectExecutor.execute(DatabaseRequest("select", databaseRequest.table, where)) as List<*>
            if (selectResult.isEmpty()) {
                return insertExecutor.execute(
                    DatabaseRequest(
                        "insert", databaseRequest.table,
                        extractAllColumnValuePairs(databaseRequest.columnValuePairs, where)
                    )
                )
            }
        }
        return super.execute(databaseRequest)
    }

    override fun composeQuery(request: DatabaseRequest, columnValuePairs: Map<String, Any?>?): String {
        val queryBuilder = StringBuilder()
        queryBuilder.append("UPDATE ")
        queryBuilder.append(request.table)
        queryBuilder.append(" SET ")
        queryBuilder.append(
            columnValuePairs!!.entries
                .filter { it.key != WHERE }
                .joinToString(",") { it.key + "=" + DEFAULT_VALUE_PLACEHOLDER })
        queryBuilder.append(" WHERE ")
        queryBuilder.append(
            getWhereCriteria(request)
                .map { "${it.key}=$DEFAULT_VALUE_PLACEHOLDER" }
                .joinToString(","))
        return queryBuilder.toString()
    }

    override fun convertToRawParameters(columnValuePairs: Map<String, Any?>): List<Any?> {
        return extractAllColumnValuePairs(columnValuePairs, columnValuePairs[WHERE]).values
            .map { getParameterValue(it) }
            .toList()
    }

    private fun extractAllColumnValuePairs(columnValuePairs: Map<String, Any?>?, where: Any?): MutableMap<String, Any?> {
        val columnValuePairsToInsert = columnValuePairs!!
            .filter { it.key != WHERE }
            .toMutableMap()
        (where as Map<*, *>?)?.let { it.forEach { (k, v) -> columnValuePairsToInsert[k.toString()] = v } }
        return columnValuePairsToInsert
    }

    private fun getWhereCriteria(databaseRequest: DatabaseRequest): Map<String, Any?> =
        (databaseRequest.columnValuePairs
            ?: error("Cannot update empty data in table " + databaseRequest.table))[WHERE] as Map<String, Any?>?
            ?: error(
                "Update operation for table ${databaseRequest.table} " +
                        "must have 'where' clause to understand which records to update"
            )
}