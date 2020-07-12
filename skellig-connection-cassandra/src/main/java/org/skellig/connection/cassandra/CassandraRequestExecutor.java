package org.skellig.connection.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.skellig.connection.cassandra.model.CassandraDetails;
import org.skellig.connection.database.DatabaseRequestExecutor;
import org.skellig.connection.database.model.DatabaseRequest;

public class CassandraRequestExecutor implements DatabaseRequestExecutor {

    private CassandraRequestExecutorFactory factory;
    private Cluster cluster;
    private Session session;

    public CassandraRequestExecutor(CassandraDetails cassandraDetails) {
        cluster = Cluster.builder()
                .addContactPointsWithPorts(cassandraDetails.getNodes())
                .build();
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
