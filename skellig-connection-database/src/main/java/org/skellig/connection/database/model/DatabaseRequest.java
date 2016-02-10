package org.skellig.connection.database.model;

import java.util.Map;
import java.util.Optional;

public class DatabaseRequest {

    private String command;
    private String table;
    private String query;
    private Map<String, Object> columnValuePairs;

    public DatabaseRequest(String command, String table, Map<String, Object> columnValuePairs) {
        this.command = command;
        this.table = table;
        this.columnValuePairs = columnValuePairs;
    }

    public DatabaseRequest(String query) {
        this.query = query;
    }

    public String getCommand() {
        return command;
    }

    public String getTable() {
        return table;
    }

    public Optional<String> getQuery() {
        return Optional.ofNullable(query);
    }

    public Optional<Map<String, Object>> getColumnValuePairs() {
        return Optional.ofNullable(columnValuePairs);
    }
}

