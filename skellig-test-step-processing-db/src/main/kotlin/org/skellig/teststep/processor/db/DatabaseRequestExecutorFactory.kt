package org.skellig.teststep.processor.db

import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processor.db.model.DatabaseRequest


/**
 * The DatabaseRequestExecutorFactory class is responsible for creating and providing [DatabaseRequestExecutor] instances
 * based on the specified command or query in a [DatabaseRequest] object.
 *
 * @property select The [DatabaseRequestExecutor] instance for the 'select' command.
 * @property insert The [DatabaseRequestExecutor] instance for the 'insert' command.
 * @property update The [DatabaseRequestExecutor] instance for the 'update' command.
 *
 * @constructor Creates a DatabaseRequestExecutorFactory object with the specified [DatabaseRequestExecutor] instances for
 * 'select', 'insert', and 'update'.
 */
open class DatabaseRequestExecutorFactory(
    select: DatabaseRequestExecutor,
    insert: DatabaseRequestExecutor,
    update: DatabaseRequestExecutor
) {

    private var databaseRequestExecutors =
        mutableMapOf(
            Pair("select", select),
            Pair("insert", insert),
            Pair("update", update)
        )

    /**
     * Retrieves the [DatabaseRequestExecutor] associated with the given DatabaseRequest.
     *
     * @param databaseRequest The DatabaseRequest specifying the query or command.
     * @return The [DatabaseRequestExecutor] associated with the query or command in the [DatabaseRequest].
     * @throws TestStepProcessingException if no [DatabaseRequestExecutor] is found for the query or command.
     */
    operator fun get(databaseRequest: DatabaseRequest): DatabaseRequestExecutor? {
        var command = databaseRequest.command
        if (isQueryOnlyProvided(databaseRequest, command)) {
            val query: String = databaseRequest.query!!
            command = databaseRequestExecutors.keys.firstOrNull { item: String -> query.lowercase().trim { it <= ' ' }.startsWith(item) }
        }
        return if (databaseRequestExecutors.containsKey(command)) {
            databaseRequestExecutors[command]
        } else {
            if (isQueryOnlyProvided(databaseRequest, command)) {
                throw TestStepProcessingException(
                    "No database query executors found for query: '${databaseRequest.query}'." +
                            " Supported types of queries: ${databaseRequestExecutors.keys}"
                )
            } else {
                throw TestStepProcessingException(
                    "No database query executors found for command: '$command'." +
                            " Supported commands: ${databaseRequestExecutors.keys}"
                )
            }
        }
    }

    private fun isQueryOnlyProvided(databaseRequest: DatabaseRequest, command: String?): Boolean {
        return command == null && databaseRequest.query != null
    }
}