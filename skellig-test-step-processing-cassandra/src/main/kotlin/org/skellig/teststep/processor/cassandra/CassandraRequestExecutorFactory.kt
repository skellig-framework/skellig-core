package org.skellig.teststep.processor.cassandra;

import com.datastax.driver.core.Session;
import org.skellig.teststep.processor.db.DatabaseRequestExecutorFactory;

class CassandraRequestExecutorFactory extends DatabaseRequestExecutorFactory {

    CassandraRequestExecutorFactory(Session session) {
        super(new CassandraSelectRequestExecutor(session), new CassandraInsertRequestExecutor(session));
    }

}
