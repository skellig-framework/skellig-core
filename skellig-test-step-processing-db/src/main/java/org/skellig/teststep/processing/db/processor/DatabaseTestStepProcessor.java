package org.skellig.teststep.processing.db.processor;

import org.skellig.connection.database.DatabaseChannel;
import org.skellig.connection.database.model.DatabaseChannelDetails;
import org.skellig.connection.database.model.DatabaseRequest;
import org.skellig.teststep.processing.db.model.DatabaseTestStep;
import org.skellig.teststep.processing.processor.BaseTestStepProcessor;
import org.skellig.teststep.processing.processor.TestStepProcessor;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.validation.TestStepResultValidator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DatabaseTestStepProcessor extends BaseTestStepProcessor<DatabaseTestStep> {

    private Map<String, DatabaseChannel> dbServers;

    protected DatabaseTestStepProcessor(Map<String, DatabaseChannel> dbServers, TestScenarioState testScenarioState, TestStepResultValidator validator) {
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
                    DatabaseChannel databaseChannel = dbServers.get(serverName);
                    DatabaseRequest request;
                    if (testStep.getQuery() != null) {
                        request = new DatabaseRequest(testStep.getQuery());
                    } else {
                        request = new DatabaseRequest(testStep.getCommand(), testStep.getTable(), (Map<String, Object>) testStep.getTestData());
                    }

                    Optional<Object> response = databaseChannel.send(request);

                    result.put(serverName, response.orElse(null));
                });
        return result.size() == 1 ? result.values().stream().findFirst().orElse(null) : result;
    }

    @Override
    public Class<DatabaseTestStep> getTestStepClass() {
        return DatabaseTestStep.class;
    }

    @Override
    public void close() {
        dbServers.values().forEach(DatabaseChannel::close);
    }

    public static class Builder extends BaseTestStepProcessor.Builder<DatabaseTestStep> {
        private Map<String, DatabaseChannel> dbServers;

        public Builder() {
            dbServers = new HashMap<>();
        }

        public Builder withDbServer(String name, DatabaseChannelDetails databaseChannelDetails) {
            dbServers.put(name, new DatabaseChannel(databaseChannelDetails));
            return this;
        }

        public TestStepProcessor<DatabaseTestStep> build() {
            return new DatabaseTestStepProcessor(dbServers, testScenarioState, validator);
        }
    }
}
