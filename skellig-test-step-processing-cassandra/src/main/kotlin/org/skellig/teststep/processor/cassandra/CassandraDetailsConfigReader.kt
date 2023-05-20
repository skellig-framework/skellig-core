package org.skellig.teststep.processor.cassandra

import com.typesafe.config.Config
import org.skellig.teststep.processor.cassandra.model.CassandraDetails
import java.net.InetSocketAddress
import java.util.*

internal class CassandraDetailsConfigReader {

    companion object {
        private const val CASSANDRA_CONFIG_KEYWORD = "cassandra.servers"
    }

    fun read(config: Config): Collection<CassandraDetails> {
        Objects.requireNonNull(config, "Cassandra config cannot be null")

        var cassandraDetails = emptyList<CassandraDetails>()
        if (config.hasPath(CASSANDRA_CONFIG_KEYWORD)) {
            val anyRefList = config.getAnyRefList(CASSANDRA_CONFIG_KEYWORD) as List<Map<*, *>>
            cassandraDetails = anyRefList
                    .map { createCassandraDetails(it) }
                    .toList()
        }
        return cassandraDetails
    }

    private fun createCassandraDetails(rawCassandraDetails: Map<*, *>): CassandraDetails {
        val server = rawCassandraDetails["server"] ?: error("Server name must be declared for Cassandra instance")
        val userName = rawCassandraDetails["userName"] as String?
        val password = rawCassandraDetails["password"] as String?
        val datacenter = rawCassandraDetails["datacenter"] as String?
        val nodes = createNodes(rawCassandraDetails)

        return CassandraDetails(server as String, nodes, datacenter, userName, password)
    }

    private fun createNodes(rawExchangesDetails: Map<*, *>): Collection<InetSocketAddress> {
        val nodes = rawExchangesDetails["nodes"] ?: error("No nodes were declared for Cassandra instance")

        return (nodes as List<Map<*, *>>)
                .map { createNode(it) }
                .toList()
    }

    private fun createNode(rawExchangeDetails: Map<*, *>): InetSocketAddress {
        val host = rawExchangeDetails["host"] ?: error("Host was not declared for Cassandra node")
        val port = rawExchangeDetails["port"] ?: error("Port was not declared for Cassandra node")

        return InetSocketAddress(host as String, port as Int)
    }
}