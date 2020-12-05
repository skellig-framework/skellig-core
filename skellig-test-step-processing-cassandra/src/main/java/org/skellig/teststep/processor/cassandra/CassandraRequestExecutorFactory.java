package org.skellig.teststep.processor.cassandra;

import com.datastax.driver.core.Session;
import org.skellig.teststep.processor.cassandra.model.CassandraDetails;
import org.skellig.teststep.processor.db.DatabaseRequestExecutor;
import org.skellig.teststep.processor.db.DatabaseRequestExecutorFactory;
import org.skellig.teststep.processor.db.model.DatabaseDetails;

class CassandraRequestExecutorFactory extends DatabaseRequestExecutorFactory {

    CassandraRequestExecutorFactory(Session session) {
        super(new CassandraSelectRequestExecutor(session), new CassandraInsertRequestExecutor(session));
    }

    @Override
    public DatabaseRequestExecutor create(DatabaseDetails databaseDetails) {
        return new CassandraRequestExecutor((CassandraDetails) databaseDetails);
    }
}
