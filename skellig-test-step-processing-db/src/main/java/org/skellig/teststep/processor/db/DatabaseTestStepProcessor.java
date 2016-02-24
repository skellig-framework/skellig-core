package org.skellig.teststep.processor.db;

import org.skellig.connection.database.DatabaseRequestExecutor;
import org.skellig.connection.database.model.DatabaseRequest;
import org.skellig.teststep.processing.exception.TestStepProcessingException;
import org.skellig.teststep.processing.processor.BaseTestStepProcessor;
import org.skellig.teststep.processing.processor.TestStepProcessor;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.validation.TestStepResultValidator;
import org.skellig.teststep.processor.db.model.DatabaseTestStep;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DatabaseTestStepProcessor extends BaseTestStepProcessor<DatabaseTestStep> {

    private Map<String, DatabaseRequestExecutor> dbServers;

    protected DatabaseTestStepProcessor(Map<String, DatabaseRequestExecutor> dbServers, TestScenarioState testScenarioState, TestStepResultValidator validator) {
        super(testScenarioState, validator);
        this.dbServers = dbServers;
    }

    @Override
    protected Object processTestStep(DatabaseTestStep testStep) {
        Map<String, Object> result = new HashMap<>();
        Collection<String> servers = testStep.getServers();
        if (servers == null || servers.isEmpty()) {
            servers = dbServers.keySet();
        }

        servers.parallelStream()
                .forEach(serverName -> {
                    DatabaseRequestExecutor databaseChannel = getDatabaseChannel(serverName);
                    DatabaseRequest request;
                    if (testStep.getQuery() != null) {
                        request = new DatabaseRequest(testStep.getQuery());
                    } else {
                        request = new DatabaseRequest(testStep.getCommand(), testStep.getTable(), (Map<String, Object>) testStep.getTestData());
                    }

                    Object response = databaseChannel.execute(request);

                    result.put(serverName, response);
                });
        return result.size() == 1 ? result.values().stream().findFirst().orElse(null) : result;
    }

    private DatabaseRequestExecutor getDatabaseChannel(String serverName) {
        if (!dbServers.containsKey(serverName)) {
            throw new TestStepProcessingException(String.format("No database channel was registered for server name '%s'", serverName));
        }
        return dbServers.get(serverName);
    }

    @Override
    public Class<DatabaseTestStep> getTestStepClass() {
        return DatabaseTestStep.class;
    }

    @Override
    public void close() {
        dbServers.values().forEach(DatabaseRequestExecutor::close);
    }

    public static class Builder extends BaseTestStepProcessor.Builder<DatabaseTestStep> {
        private Map<String, DatabaseRequestExecutor> dbServers;

        public Builder() {
            dbServers = new HashMap<>();
        }

        public Builder withDbServer(String name, DatabaseRequestExecutor databaseRequestExecutor) {
            dbServers.put(name, databaseRequestExecutor);
            return this;
        }

        public TestStepProcessor<DatabaseTestStep> build() {
            return new DatabaseTestStepProcessor(dbServers, testScenarioState, validator);
        }
    }
}
