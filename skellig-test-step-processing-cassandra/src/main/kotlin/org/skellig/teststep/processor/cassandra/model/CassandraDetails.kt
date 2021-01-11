package org.skellig.teststep.processor.cassandra.model

import org.skellig.teststep.processor.db.model.DatabaseDetails
import java.net.InetSocketAddress

class CassandraDetails(serverName: String,
                       val nodes: Collection<InetSocketAddress>,
                       userName: String?,
                       password: String?) : DatabaseDetails(serverName, userName, password)