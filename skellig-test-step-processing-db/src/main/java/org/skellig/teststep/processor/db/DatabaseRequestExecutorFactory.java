package org.skellig.teststep.processor.db;

import org.skellig.teststep.processing.exception.TestStepProcessingException;
import org.skellig.teststep.processor.db.model.DatabaseRequest;

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
                    .filter(item -> query.toLowerCase().trim().startsWith(item))
                    .findFirst()
                    .orElse(null);
        }

        if (databaseRequestExecutors.containsKey(command)) {
            return databaseRequestExecutors.get(command);
        } else {
            if (isQueryOnlyProvided(databaseRequest, command)) {
                throw new TestStepProcessingException("No database query executors found for query: " + databaseRequest.getQuery());
            } else {
                throw new TestStepProcessingException("No database query executors found for command: " + command);
            }
        }
    }

    private boolean isQueryOnlyProvided(DatabaseRequest databaseRequest, String command) {
        return command == null && databaseRequest.getQuery() != null;
    }
}