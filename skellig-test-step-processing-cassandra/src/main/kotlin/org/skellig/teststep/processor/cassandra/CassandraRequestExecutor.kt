package org.skellig.teststep.processor.cassandra

import com.datastax.oss.driver.api.core.CqlSession
import org.skellig.teststep.processor.cassandra.model.CassandraDetails
import org.skellig.teststep.processor.db.DatabaseRequestExecutor
import org.skellig.teststep.processor.db.model.DatabaseRequest

class CassandraRequestExecutor(cassandraDetails: CassandraDetails) : DatabaseRequestExecutor {

    private val factory: CassandraRequestExecutorFactory
    private val session: CqlSession

    init {
        val sessionBuilder = CqlSession.builder().addContactPoints(cassandraDetails.nodes)
        cassandraDetails.userName?.let {
            sessionBuilder.withAuthCredentials(cassandraDetails.userName, cassandraDetails.password?:"")
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