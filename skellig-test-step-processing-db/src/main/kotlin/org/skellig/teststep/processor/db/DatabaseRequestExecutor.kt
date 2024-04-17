package org.skellig.teststep.processor.db

import org.skellig.teststep.processor.db.model.DatabaseRequest
import java.io.Closeable

/**
 * This interface provides a contract for executing database requests.
 * Implementing classes must implement the [execute] method to execute a database request.
 * The [close] method should be implemented if any resources need to be released after executing the request.
 *
 * @see Closeable
 */
interface DatabaseRequestExecutor : Closeable {

    /**
     * Executes a database request.
     *
     * @param databaseRequest The [DatabaseRequest] object representing the database request to be executed.
     * @return The result of the database request execution.
     */
    fun execute(databaseRequest: DatabaseRequest): Any?

    /**
     * Closes the [DatabaseRequestExecutor] and releases any resources or connections associated with it.
     * This method should be called after executing a database request and when the [DatabaseRequestExecutor] is no longer needed.
     */
    override fun close()
}