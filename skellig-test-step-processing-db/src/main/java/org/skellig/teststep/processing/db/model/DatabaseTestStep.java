package org.skellig.teststep.processing.db.model;

import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.model.TestStepExecutionType;
import org.skellig.teststep.processing.model.ValidationDetails;

import java.util.Collection;
import java.util.Map;

public class DatabaseTestStep extends TestStep {

    private Collection<String> servers;
    private String command;
    private String table;
    private String query;

    protected DatabaseTestStep(String id, String name, TestStepExecutionType execution, int timeout, int delay,
                               Map<String, Object> variables, Object testData, ValidationDetails validationDetails,
                               Collection<String> servers, String command, String table, String query) {
        super(id, name, execution, timeout, delay, variables, testData, validationDetails);
        this.servers = servers;
        this.command = command;
        this.table = table;
        this.query = query;
    }


    public Collection<String> getServers() {
        return servers;
    }

    public String getCommand() {
        return command;
    }

    public String getTable() {
        return table;
    }

    public String getQuery() {
        return query;
    }

    public static class Builder extends TestStep.Builder {

        private Collection<String> servers;
        private String command;
        private String table;
        private String query;

        public Builder withServers(Collection<String> servers) {
            this.servers = servers;
            return this;
        }

        public Builder withCommand(String command) {
            this.command = command;
            return this;
        }

        public Builder withTable(String table) {
            this.table = table;
            return this;
        }

        public Builder withQuery(String query) {
            this.query = query;
            return this;
        }

        @Override
        public DatabaseTestStep build() {
            return new DatabaseTestStep(id, name, execution, timeout, delay, variables, testData,
                    validationDetails, servers, command, table, query);
        }
    }
}
