package org.skellig.connection.database;

import org.skellig.connection.channel.SendingChannel;
import org.skellig.connection.database.exception.DatabaseChannelException;
import org.skellig.connection.database.model.DatabaseChannelDetails;
import org.skellig.connection.database.model.DatabaseRequest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

public class DatabaseChannel implements SendingChannel {

    private Connection connection;
    private DatabaseRequestExecutorFactory factory;

    public DatabaseChannel(DatabaseChannelDetails details) {
        connectToDatabase(details);
        factory = new DatabaseRequestExecutorFactory();
    }

    @Override
    public Optional<Object> send(Object request) {
        DatabaseRequest databaseRequest = (DatabaseRequest) request;

        BaseDatabaseRequestExecutor databaseRequestExecutor = factory.get(databaseRequest);

        return Optional.ofNullable(databaseRequestExecutor.executeRequest(connection, databaseRequest));
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            //log later
        }
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
}
