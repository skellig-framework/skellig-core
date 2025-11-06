package org.skellig.teststep.processor.cassandra

import com.datastax.oss.driver.api.core.CqlSession
import org.skellig.teststep.processing.util.debug
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processor.cassandra.model.CassandraDetails
import org.skellig.teststep.processor.db.DatabaseRequestExecutor
import org.skellig.teststep.processor.db.model.DatabaseRequest

/**
 * Represents a Cassandra request executor for executing database requests.
 * It connects to Cassandra DB when object is created.
 *
 * @property cassandraDetails The Cassandra details for connecting to the database server.
 */
class CassandraRequestExecutor(private val cassandraDetails: CassandraDetails) : DatabaseRequestExecutor {

    private val log = logger<CassandraRequestExecutor>()
    private val sessionInit = lazy {
        log.debug { "Initialize Cassandra Request Executor and Session for server '${cassandraDetails.serverName}'" }
        val sessionBuilder = CqlSession.builder()
            .withLocalDatacenter(cassandraDetails.datacenter ?: "datacenter1")
            .addContactPoints(cassandraDetails.nodes)
        cassandraDetails.userName?.let {
            sessionBuilder.withAuthCredentials(it, cassandraDetails.password ?: "")
        }
        sessionBuilder.build()
    }
    private val session: CqlSession by sessionInit
    private val factory: CassandraRequestExecutorFactory by lazy {
        CassandraRequestExecutorFactory(session)
    }

    override fun execute(databaseRequest: DatabaseRequest): Any? {
        return factory[databaseRequest]?.execute(databaseRequest)
    }

    /**
     * Closes the session associated with the CassandraRequestExecutor.
     * It should be called after executing a database request and when the CassandraRequestExecutor is no longer needed.
     */
    override fun close() {
        if (sessionInit.isInitialized()) {
            log.debug("Close Database connection to $cassandraDetails")
            session.close()
        }
    }
}