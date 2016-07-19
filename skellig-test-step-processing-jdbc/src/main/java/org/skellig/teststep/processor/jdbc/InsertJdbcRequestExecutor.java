package org.skellig.teststep.processor.jdbc;

import org.skellig.teststep.processor.db.exception.DatabaseChannelException;
import org.skellig.teststep.processor.db.model.DatabaseRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

class InsertJdbcRequestExecutor extends BaseJdbcRequestExecutor {

    private Connection connection;

    InsertJdbcRequestExecutor(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Object execute(DatabaseRequest databaseRequest) {
        int result;
        try {
            String query;
            if (databaseRequest.getQuery() != null) {
                query = databaseRequest.getQuery();
                result = connection.createStatement().executeUpdate(query);
            } else {
                Map<String, Object> insertData = databaseRequest.getColumnValuePairs().orElse(Collections.emptyMap());
                query = composeInsertQuery(databaseRequest, insertData);

                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    Object[] rawParameters = convertToRawParameters(insertData);
                    for (int i = 0; i < rawParameters.length; i++) {
                        preparedStatement.setObject(i + 1, rawParameters[i]);
                    }
                    result = preparedStatement.executeUpdate();
                }
            }
            connection.commit();
        } catch (Exception ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                //log later
            }
            throw new DatabaseChannelException(ex.getMessage(), ex);
        }
        return result;
    }

    private String composeInsertQuery(DatabaseRequest request, Map<String, Object> columnValuePairs) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(request.getCommand());
        queryBuilder.append(" INTO ");
        queryBuilder.append(request.getTable());
        queryBuilder.append(" (");
        appendColumns(columnValuePairs, queryBuilder);
        queryBuilder.append(") VALUES(");
        appendValues(columnValuePairs, queryBuilder);
        queryBuilder.append(")");

        return queryBuilder.toString();
    }

    private void appendColumns(Map<String, Object> columnValuePairs, StringBuilder queryBuilder) {
        queryBuilder.append(String.join(",", columnValuePairs.keySet()));
    }

    private Object[] convertToRawParameters(Map<String, Object> columnValuePairs) {
        return columnValuePairs.values().stream()
                .map(this::getParameterValue)
                .toArray();
    }

    private void appendValues(Map<String, Object> columnValuePairs, StringBuilder queryBuilder) {
        queryBuilder.append(columnValuePairs.values().stream()
                .map(column -> "?")
                .collect(Collectors.joining(",")));
    }
}
