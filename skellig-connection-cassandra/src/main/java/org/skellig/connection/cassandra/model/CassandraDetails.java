package org.skellig.connection.cassandra.model;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Optional;

public class CassandraDetails {

    private Collection<InetSocketAddress> nodes;
    private String username;
    private String password;

    public CassandraDetails(Collection<InetSocketAddress> nodes) {
        this.nodes = nodes;
    }

    public CassandraDetails(Collection<InetSocketAddress> nodes, String username, String password) {
        this.nodes = nodes;
        this.username = username;
        this.password = password;
    }

    public Collection<InetSocketAddress> getNodes() {
        return nodes;
    }

    public Optional<String> getUsername() {
        return Optional.ofNullable(username);
    }

    public Optional<String> getPassword() {
        return Optional.ofNullable(password);
    }
}
