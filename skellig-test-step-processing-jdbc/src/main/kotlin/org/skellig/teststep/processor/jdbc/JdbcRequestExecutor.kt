package org.skellig.teststep.processor.jdbc

import org.skellig.teststep.processing.exception.TestDataProcessingInitException
import org.skellig.teststep.processor.db.DatabaseRequestExecutor
import org.skellig.teststep.processor.db.model.DatabaseRequest
import org.skellig.teststep.processor.jdbc.model.JdbcDetails
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class JdbcRequestExecutor(details: JdbcDetails) : DatabaseRequestExecutor {

    private val factory: JdbcRequestExecutorFactory
    private var connection: Connection? = null

    init {
        connectToDatabase(details)
        factory = JdbcRequestExecutorFactory(connection)
    }

    override fun execute(request: DatabaseRequest): Any? {
        return factory[request]!!.execute(request)
    }

    private fun connectToDatabase(details: JdbcDetails) {
        try {
            Class.forName(details.driverName)
            connection = DriverManager.getConnection(details.url, details.userName, details.password)
            connection!!.autoCommit = false
        } catch (e: Exception) {
            throw TestDataProcessingInitException("Failed to connect to DB ${details.url}. Reason: ${e.message}", e)
        }
    }

    override fun close() {
        try {
            connection!!.close()
        } catch (e: SQLException) {
            //log later
        }
    }
}