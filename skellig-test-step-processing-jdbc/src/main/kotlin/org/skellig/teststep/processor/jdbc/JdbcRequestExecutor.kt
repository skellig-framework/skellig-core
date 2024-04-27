package org.skellig.teststep.processor.jdbc

import org.skellig.teststep.processing.exception.TestStepProcessorInitException
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processor.db.DatabaseRequestExecutor
import org.skellig.teststep.processor.db.model.DatabaseRequest
import org.skellig.teststep.processor.jdbc.model.JdbcDetails
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

/**
 * Represents a JDBC request executor for executing database requests.
 * It connects to the DB when object is created.
 *
 * @property details The JDBC details for connecting to the database server.
 */
class JdbcRequestExecutor(details: JdbcDetails) : DatabaseRequestExecutor {

    private val log = logger<JdbcRequestExecutor>()
    private var factory: JdbcRequestExecutorFactory
    private var connection: Connection? = null

    init {
        connectToDatabase(details)
        factory = JdbcRequestExecutorFactory(connection!!)
    }

    override fun execute(databaseRequest: DatabaseRequest): Any? {
        return factory[databaseRequest]!!.execute(databaseRequest)
    }

    private fun connectToDatabase(details: JdbcDetails) {
        try {
            Class.forName(details.driverName)
            connection = DriverManager.getConnection(details.url, details.userName, details.password)
            connection!!.autoCommit = false
            connection!!.transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
        } catch (e: Exception) {
            throw TestStepProcessorInitException("Failed to connect to DB ${details.url}. Reason: ${e.message}", e)
        }
    }

    /**
     * Closes the JDBC connection associated with the JdbcRequestExecutor.
     * It releases any resources or connections associated with it.
     * This method should be called after executing a database request and when the JdbcRequestExecutor is no longer needed.
     */
    override fun close() {
        try {
            connection!!.close()
        } catch (e: SQLException) {
            log.error("Failed to closed JDBC connection. Reason: " + e.message)
        }
    }
}