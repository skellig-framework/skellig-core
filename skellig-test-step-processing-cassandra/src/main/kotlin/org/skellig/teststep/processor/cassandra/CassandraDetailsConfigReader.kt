package org.skellig.teststep.processor.cassandra;

import com.typesafe.config.Config;
import org.skellig.teststep.processor.cassandra.model.CassandraDetails;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

class CassandraDetailsConfigReader {

    private static final String CASSANDRA_CONFIG_KEYWORD = "cassandra";

    Collection<CassandraDetails> read(Config config) {
        Objects.requireNonNull(config, "Cassandra config cannot be null");

        Collection<CassandraDetails> cassandraDetails = Collections.emptyList();
        if (config.hasPath(CASSANDRA_CONFIG_KEYWORD)) {
            List<Map> anyRefList = (List<Map>) config.getAnyRefList(CASSANDRA_CONFIG_KEYWORD);
            cassandraDetails = anyRefList.stream()
                    .map(this::createCassandraDetails)
                    .collect(Collectors.toList());
        }
        return cassandraDetails;
    }

    private CassandraDetails createCassandraDetails(Map rawCassandraDetails) {
        String server = (String) rawCassandraDetails.get("server");
        String userName = (String) rawCassandraDetails.get("userName");
        String password = (String) rawCassandraDetails.get("password");
        Collection<InetSocketAddress> nodes = createNodes(rawCassandraDetails);

        Objects.requireNonNull(server, "Server name must be declared for Cassandra instance");

        return new CassandraDetails(server, nodes, userName, password);
    }

    private Collection<InetSocketAddress> createNodes(Map rawExchangesDetails) {
        List<Map> nodes = (List<Map>) rawExchangesDetails.get("nodes");

        Objects.requireNonNull(nodes, "No nodes were declared for Cassandra instance");

        return nodes.stream()
                .map(this::createNode)
                .collect(Collectors.toList());
    }

    private InetSocketAddress createNode(Map rawExchangeDetails) {
        String host = (String) rawExchangeDetails.get("host");
        Integer port = (Integer) rawExchangeDetails.get("port");

        Objects.requireNonNull(host, "Host was not declared for Cassandra node");
        Objects.requireNonNull(port, "Port was not declared for Cassandra node");

        return new InetSocketAddress(host, port);
    }

}
