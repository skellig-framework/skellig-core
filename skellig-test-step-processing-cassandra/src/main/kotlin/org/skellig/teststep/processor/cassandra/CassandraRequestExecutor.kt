package org.skellig.teststep.processor.cassandra

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.PlainTextAuthProvider
import com.datastax.driver.core.Session
import org.skellig.teststep.processor.cassandra.model.CassandraDetails
import org.skellig.teststep.processor.db.DatabaseRequestExecutor
import org.skellig.teststep.processor.db.model.DatabaseRequest

class CassandraRequestExecutor(cassandraDetails: CassandraDetails) : DatabaseRequestExecutor {

    private val factory: CassandraRequestExecutorFactory
    private val cluster: Cluster
    private val session: Session

    init {
        val clusterBuilder = Cluster.builder().addContactPointsWithPorts(cassandraDetails.nodes)
        cassandraDetails.userName?.let {
            clusterBuilder.withAuthProvider(
                    PlainTextAuthProvider(cassandraDetails.userName, cassandraDetails.password))
        }
        cluster = clusterBuilder.build()
        session = cluster.connect()
        factory = CassandraRequestExecutorFactory(session)
    }

    override fun execute(databaseRequest: DatabaseRequest): Any? {
        return factory[databaseRequest]?.execute(databaseRequest)
    }

    override fun close() {
        session.close()
        cluster.close()
    }
}