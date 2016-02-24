package org.skellig.connection.database;

import org.skellig.connection.database.model.DatabaseRequest;

public interface DatabaseRequestExecutor extends AutoCloseable {

    Object execute(DatabaseRequest databaseRequest);

    @Override
    void close();
}
