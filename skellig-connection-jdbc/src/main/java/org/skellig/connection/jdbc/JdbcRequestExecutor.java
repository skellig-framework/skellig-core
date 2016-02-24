package org.skellig.connection.jdbc;

import org.skellig.connection.database.DatabaseRequestExecutor;
import org.skellig.connection.database.exception.DatabaseChannelException;
import org.skellig.connection.database.model.DatabaseRequest;
import org.skellig.connection.database.model.JdbcDetails;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdbcRequestExecutor implements DatabaseRequestExecutor {

    private final JdbcRequestExecutorFactory factory;

    private Connection connection;

    public JdbcRequestExecutor(JdbcDetails details) {
        connectToDatabase(details);
        factory = new JdbcRequestExecutorFactory(connection);
    }

    @Override
    public Object execute(DatabaseRequest request) {
        return factory.get(request).execute(request);
    }

    private void connectToDatabase(JdbcDetails details) {
        try {
            Class.forName(details.getDriverName());
            connection = DriverManager.getConnection(details.getUrl(), details.getUserName(), details.getPassword());
            connection.setAutoCommit(false);
        } catch (Exception e) {
            throw new DatabaseChannelException(e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            //log later
        }
    }
}
