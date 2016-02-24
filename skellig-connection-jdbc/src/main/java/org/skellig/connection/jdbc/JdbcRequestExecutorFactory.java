package org.skellig.connection.jdbc;

import org.skellig.connection.database.DatabaseRequestExecutorFactory;

import java.sql.Connection;

class JdbcRequestExecutorFactory extends DatabaseRequestExecutorFactory {

    public JdbcRequestExecutorFactory(Connection connection) {
        super(new FindJdbcRequestExecutor(connection), new InsertJdbcRequestExecutor(connection));
    }

}
