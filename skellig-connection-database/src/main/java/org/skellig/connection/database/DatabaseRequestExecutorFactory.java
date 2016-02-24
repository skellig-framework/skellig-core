package org.skellig.connection.database;

import org.skellig.connection.database.exception.DatabaseChannelException;
import org.skellig.connection.database.model.DatabaseRequest;

import java.util.HashMap;
import java.util.Map;

public class DatabaseRequestExecutorFactory {

    private Map<String, DatabaseRequestExecutor> databaseRequestExecutors;

    public DatabaseRequestExecutorFactory(DatabaseRequestExecutor select,
                                          DatabaseRequestExecutor insert) {
        databaseRequestExecutors = new HashMap<>();
        databaseRequestExecutors.put("select", select);
        databaseRequestExecutors.put("insert", insert);
    }

    public DatabaseRequestExecutor get(DatabaseRequest databaseRequest) {
        String command = databaseRequest.getCommand();
        if (isQueryOnlyProvided(databaseRequest, command)) {
            String query = databaseRequest.getQuery();
            command = databaseRequestExecutors.keySet().stream()
                    .filter(item -> query.toLowerCase().contains(item))
                    .findFirst()
                    .orElse(null);
        }

        if (databaseRequestExecutors.containsKey(command)) {
            return databaseRequestExecutors.get(command);
        } else {
            if (isQueryOnlyProvided(databaseRequest, command)) {
                throw new DatabaseChannelException("No database query executors found for query: " + databaseRequest.getQuery());
            } else {
                throw new DatabaseChannelException("No database query executors found for command: " + command);
            }
        }
    }

    private boolean isQueryOnlyProvided(DatabaseRequest databaseRequest, String command) {
        return command == null && databaseRequest.getQuery() != null;
    }
}
