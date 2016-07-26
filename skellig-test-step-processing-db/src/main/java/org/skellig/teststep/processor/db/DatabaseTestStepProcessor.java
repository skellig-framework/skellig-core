package org.skellig.teststep.processor.db;

import com.typesafe.config.Config;
import org.skellig.teststep.processing.converter.TestStepResultConverter;
import org.skellig.teststep.processing.exception.TestStepProcessingException;
import org.skellig.teststep.processing.processor.BaseTestStepProcessor;
import org.skellig.teststep.processing.processor.TestStepProcessor;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.validation.TestStepResultValidator;
import org.skellig.teststep.processor.db.model.DatabaseDetails;
import org.skellig.teststep.processor.db.model.DatabaseRequest;
import org.skellig.teststep.processor.db.model.DatabaseTestStep;

import java.util.HashMap;
import java.util.Map;

public abstract class DatabaseTestStepProcessor<T extends DatabaseRequestExecutor> extends BaseTestStepProcessor<DatabaseTestStep> {

    private Map<String, T> dbServers;

    protected DatabaseTestStepProcessor(Map<String, T> dbServers,
                                        TestScenarioState testScenarioState, TestStepResultValidator validator,
                                        TestStepResultConverter testStepResultConverter) {
        super(testScenarioState, validator, testStepResultConverter);
        this.dbServers = dbServers;
    }

    @Override
    protected Object processTestStep(DatabaseTestStep testStep) {
        if (testStep.getServers().isEmpty()) {
            throw new TestStepProcessingException("No DB servers were provided to run a query." +
                    " Registered servers are: " + dbServers.keySet().toString());
        }

        Map<String, Object> result = new HashMap<>();

        testStep.getServers()
                .forEach(serverName -> {
                    Object response = getDatabaseServer(serverName).execute(getDatabaseRequest(testStep));
                    result.put(serverName, response);
                });
        return result;
    }

    private DatabaseRequest getDatabaseRequest(DatabaseTestStep testStep) {
        DatabaseRequest request;
        if (testStep.getQuery() != null) {
            request = new DatabaseRequest(testStep.getQuery());
        } else {
            request = new DatabaseRequest(testStep.getCommand(), testStep.getTable(), (Map<String, Object>) testStep.getTestData());
        }
        return request;
    }

    private DatabaseRequestExecutor getDatabaseServer(String serverName) {
        if (!dbServers.containsKey(serverName)) {
            throw new TestStepProcessingException(String.format("No database was registered for server name '%s'." +
                    " Registered servers are: %s", serverName, dbServers.keySet().toString()));
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

    public static abstract class Builder<D extends DatabaseDetails, RE extends DatabaseRequestExecutor>
            extends BaseTestStepProcessor.Builder<DatabaseTestStep> {

        protected Map<String, RE> dbServers;

        public Builder() {
            dbServers = new HashMap<>();
        }

        public Builder<D, RE> withDbServer(D databaseDetails) {
            dbServers.put(databaseDetails.getServerName(), createRequestExecutor(databaseDetails));
            return this;
        }

        public abstract Builder<D, RE> withDbServers(Config config);

        protected abstract RE createRequestExecutor(D databaseDetails);

        public abstract TestStepProcessor<DatabaseTestStep> build();
    }
}
