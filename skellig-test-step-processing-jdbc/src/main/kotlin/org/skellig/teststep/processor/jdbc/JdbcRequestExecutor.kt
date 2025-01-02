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
class JdbcRequestExecutor(private val details: JdbcDetails) : DatabaseRequestExecutor {

    private val log = logger<JdbcRequestExecutor>()
    private val factory: JdbcRequestExecutorFactory by lazy {
        JdbcRequestExecutorFactory(connection)
    }
    private val connectionInit = lazy {
        try {
            Class.forName(details.driverName)
            val dbConnection = DriverManager.getConnection(details.url, details.userName, details.password)
            dbConnection.autoCommit = false
            dbConnection.transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
            dbConnection
        } catch (e: Exception) {
            throw TestStepProcessorInitException("Failed to connect to DB ${details.url}. Reason: ${e.message}", e)
        }
    }
    private val connection: Connection by connectionInit

    override fun execute(databaseRequest: DatabaseRequest): Any? {
        return factory[databaseRequest]!!.execute(databaseRequest)
    }

    /**
     * Closes the JDBC connection associated with the JdbcRequestExecutor.
     * It releases any resources or connections associated with it.
     * This method should be called after executing a database request and when the JdbcRequestExecutor is no longer needed.
     */
    override fun close() {
        try {
            if(connectionInit.isInitialized()) {
                log.debug("Close Database connection to $details")
                connection.close()
            }
        } catch (e: SQLException) {
            log.error("Failed to closed JDBC connection. Reason: " + e.message)
        }
    }
}