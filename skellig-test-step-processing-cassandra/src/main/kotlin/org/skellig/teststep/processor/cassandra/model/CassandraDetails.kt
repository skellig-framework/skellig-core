package org.skellig.teststep.processor.cassandra.model;

import org.skellig.teststep.processor.db.model.DatabaseDetails;

import java.net.InetSocketAddress;
import java.util.Collection;

public class CassandraDetails extends DatabaseDetails {

    private Collection<InetSocketAddress> nodes;

    public CassandraDetails(String serverName, Collection<InetSocketAddress> nodes, String userName, String password) {
        super(serverName, userName, password);
        this.nodes = nodes;
    }

    public Collection<InetSocketAddress> getNodes() {
        return nodes;
    }

}
