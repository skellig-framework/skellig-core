package org.skellig.connection.database;

import org.skellig.connection.database.exception.DatabaseChannelException;
import org.skellig.connection.database.model.DatabaseChannelDetails;
import org.skellig.connection.database.model.DatabaseRequest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseChannel implements AutoCloseable {

    private Connection connection;
    private static final DatabaseRequestExecutorFactory factory = new DatabaseRequestExecutorFactory();

    public DatabaseChannel(DatabaseChannelDetails details) {
        connectToDatabase(details);
    }

    public Object send(DatabaseRequest request) {
        return factory.get(request).executeRequest(connection, request);
    }

    private void connectToDatabase(DatabaseChannelDetails details) {
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
