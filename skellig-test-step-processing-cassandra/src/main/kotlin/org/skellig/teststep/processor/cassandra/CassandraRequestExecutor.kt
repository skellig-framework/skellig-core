package org.skellig.teststep.processor.cassandra

import com.datastax.oss.driver.api.core.CqlSession
import org.skellig.teststep.processing.util.debug
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processor.cassandra.model.CassandraDetails
import org.skellig.teststep.processor.db.DatabaseRequestExecutor
import org.skellig.teststep.processor.db.model.DatabaseRequest

class CassandraRequestExecutor(cassandraDetails: CassandraDetails) : DatabaseRequestExecutor {

    private val log = logger<CassandraRequestExecutor>()
    private val factory: CassandraRequestExecutorFactory
    private val session: CqlSession

    init {
        log.debug {"Initialize Cassandra Request Executor and Session for server '${cassandraDetails.serverName}'"}
        val sessionBuilder = CqlSession.builder()
            .withLocalDatacenter(cassandraDetails.datacenter ?: "datacenter1")
            .addContactPoints(cassandraDetails.nodes)
        cassandraDetails.userName?.let {
            sessionBuilder.withAuthCredentials(it, cassandraDetails.password ?: "")
        }
        session = sessionBuilder.build();
        factory = CassandraRequestExecutorFactory(session)
    }

    override fun execute(databaseRequest: DatabaseRequest): Any? {
        return factory[databaseRequest]?.execute(databaseRequest)
    }

    override fun close() {
        session.close()
    }
}