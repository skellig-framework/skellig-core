package org.skellig.teststep.processor.jdbc;

import org.skellig.teststep.processor.db.DatabaseRequestExecutor;
import org.skellig.teststep.processor.db.DatabaseRequestExecutorFactory;
import org.skellig.teststep.processor.db.model.DatabaseDetails;
import org.skellig.teststep.processor.jdbc.model.JdbcDetails;

import java.sql.Connection;

class JdbcRequestExecutorFactory extends DatabaseRequestExecutorFactory {

    JdbcRequestExecutorFactory(Connection connection) {
        super(new FindJdbcRequestExecutor(connection), new InsertJdbcRequestExecutor(connection));
    }

    @Override
    public DatabaseRequestExecutor create(DatabaseDetails databaseDetails) {
        return new JdbcRequestExecutor((JdbcDetails) databaseDetails);
    }
}
