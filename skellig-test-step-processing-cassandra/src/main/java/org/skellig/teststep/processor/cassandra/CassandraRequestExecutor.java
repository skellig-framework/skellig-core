package org.skellig.teststep.processor.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PlainTextAuthProvider;
import com.datastax.driver.core.Session;
import org.skellig.teststep.processor.cassandra.model.CassandraDetails;
import org.skellig.teststep.processor.db.DatabaseRequestExecutor;
import org.skellig.teststep.processor.db.model.DatabaseRequest;

class CassandraRequestExecutor implements DatabaseRequestExecutor {

    private CassandraRequestExecutorFactory factory;
    private Cluster cluster;
    private Session session;

    CassandraRequestExecutor(CassandraDetails cassandraDetails) {
        Cluster.Builder clusterBuilder = Cluster.builder()
                .addContactPointsWithPorts(cassandraDetails.getNodes());
        if (cassandraDetails.getUserName().isPresent()) {
            clusterBuilder.withAuthProvider(
                    new PlainTextAuthProvider(cassandraDetails.getUserName().get(), cassandraDetails.getPassword().orElse(null)));
        }
        cluster = clusterBuilder.build();
        session = cluster.connect();

        factory = new CassandraRequestExecutorFactory(session);
    }

    @Override
    public Object execute(DatabaseRequest request) {
        return factory.get(request).execute(request);
    }

    @Override
    public void close() {
        session.close();
        cluster.close();
    }
}
