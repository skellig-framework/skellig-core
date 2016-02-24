package org.skellig.connection.cassandra;

import com.datastax.driver.core.Session;
import org.skellig.connection.database.DatabaseRequestExecutorFactory;

class CassandraRequestExecutorFactory extends DatabaseRequestExecutorFactory {

    public CassandraRequestExecutorFactory(Session session) {
        super(new CassandraSelectRequestExecutor(session), null);
    }

}
