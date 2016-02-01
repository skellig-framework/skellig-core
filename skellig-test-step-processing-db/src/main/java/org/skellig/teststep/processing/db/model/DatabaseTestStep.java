package org.skellig.teststep.processing.db.model;

import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.model.ValidationDetails;

public class DatabaseTestStep extends TestStep {

    private String command;
    private String table;
    private String query;

    protected DatabaseTestStep(String id, String name, Object testData, ValidationDetails validationDetails,
                               String command, String table, String query) {
        super(id, name, testData, validationDetails);
        this.command = command;
        this.table = table;
        this.query = query;
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

        private String command;
        private String table;
        private String query;

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
        public TestStep build() {
            return new DatabaseTestStep(id, name, testData, validationDetails, command, table, query);
        }
    }
}