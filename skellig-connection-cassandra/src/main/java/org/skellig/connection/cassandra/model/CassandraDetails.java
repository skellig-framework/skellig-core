package org.skellig.connection.cassandra.model;

import java.net.InetSocketAddress;
import java.util.Collection;

public class CassandraDetails {

    private Collection<InetSocketAddress> nodes;

    public CassandraDetails(Collection<InetSocketAddress> nodes) {
        this.nodes = nodes;
    }

    public Collection<InetSocketAddress> getNodes() {
        return nodes;
    }
}
