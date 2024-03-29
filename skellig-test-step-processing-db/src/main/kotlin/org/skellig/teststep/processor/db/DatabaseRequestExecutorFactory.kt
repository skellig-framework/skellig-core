package org.skellig.teststep.processor.db

import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processor.db.model.DatabaseRequest

open class DatabaseRequestExecutorFactory(select: DatabaseRequestExecutor,
                                          insert: DatabaseRequestExecutor,
                                          update: DatabaseRequestExecutor) {

    private var databaseRequestExecutors =
            mutableMapOf(
                    Pair("select", select),
                    Pair("insert", insert),
                    Pair("update", update)
            )

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
                throw TestStepProcessingException(java.lang.String.format("No database query executors found for query: '%s'." +
                        " Supported types of queries: %s", databaseRequest.query, databaseRequestExecutors.keys))
            } else {
                throw TestStepProcessingException(String.format("No database query executors found for command: '%s'." +
                        " Supported commands: %s", command, databaseRequestExecutors.keys))
            }
        }
    }

    private fun isQueryOnlyProvided(databaseRequest: DatabaseRequest, command: String?): Boolean {
        return command == null && databaseRequest.query != null
    }
}