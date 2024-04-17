package org.skellig.teststep.processor.cassandra.model

import org.skellig.teststep.processor.db.model.DatabaseDetails
import java.net.InetSocketAddress

/**
 * Represents the details of a Cassandra database.
 * It extends the [DatabaseDetails] class and adds the list of [nodes] and [datacenter].
 *
 * @param serverName The name of the Cassandra server.
 * @param nodes The collection of InetSocketAddress representing the Cassandra nodes.
 * @param datacenter The name of the datacenter. Can be null.
 * @param userName The username to connect to the Cassandra server. Can be null.
 * @param password The password to connect to the Cassandra server. Can be null.
 */
class CassandraDetails(
    serverName: String,
    val nodes: Collection<InetSocketAddress>,
    val datacenter: String?,
    userName: String?,
    password: String?
) : DatabaseDetails(serverName, userName, password) {

    override fun toString(): String {
        return "(${super.toString()}, nodes = $nodes, datacenter = $datacenter)"
    }
}