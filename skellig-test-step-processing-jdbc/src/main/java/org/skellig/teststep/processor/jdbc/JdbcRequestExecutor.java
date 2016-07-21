package org.skellig.teststep.processor.jdbc;

import org.skellig.teststep.processing.exception.TestStepProcessingException;
import org.skellig.teststep.processor.db.DatabaseRequestExecutor;
import org.skellig.teststep.processor.db.model.DatabaseRequest;
import org.skellig.teststep.processor.jdbc.model.JdbcDetails;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class JdbcRequestExecutor implements DatabaseRequestExecutor {

    private final JdbcRequestExecutorFactory factory;

    private Connection connection;

    JdbcRequestExecutor(JdbcDetails details) {
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
            connection = DriverManager.getConnection(details.getUrl(),
                    details.getUserName().orElse(null), details.getPassword().orElse(null));
            connection.setAutoCommit(false);
        } catch (Exception e) {
            throw new TestStepProcessingException(e.getMessage(), e);
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
