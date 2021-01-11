package org.skellig.teststep.processor.jdbc;

import org.skellig.teststep.processor.db.DatabaseRequestExecutorFactory;

import java.sql.Connection;

class JdbcRequestExecutorFactory extends DatabaseRequestExecutorFactory {

    JdbcRequestExecutorFactory(Connection connection) {
        super(new FindJdbcRequestExecutor(connection), new InsertJdbcRequestExecutor(connection));
    }
}
