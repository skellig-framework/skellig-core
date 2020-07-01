package org.skellig.connection.database;

import org.skellig.connection.database.exception.DatabaseChannelException;
import org.skellig.connection.database.model.DatabaseRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class FindDatabaseRequestExecutor extends BaseDatabaseRequestExecutor {

    private static final String COMPARATOR = "comparator";

    Object executeRequest(Connection connection, DatabaseRequest databaseRequest) {
        try {
            String query;
            if (databaseRequest.getQuery() != null) {
                query = databaseRequest.getQuery();
                return executeQuery(query, connection.createStatement());
            } else {
                Map<String, Object> searchCriteria = databaseRequest.getColumnValuePairs().orElse(Collections.emptyMap());
                query = composeFindQuery(databaseRequest, searchCriteria);

                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    Object[] rawParameters = convertToRawParameters(searchCriteria);
                    for (int i = 0; i < rawParameters.length; i++) {
                        preparedStatement.setObject(i + 1, rawParameters[i]);
                    }
                    return executeQuery(preparedStatement);
                }
            }
        } catch (Exception ex) {
            throw new DatabaseChannelException(ex.getMessage(), ex);
        }
    }

    private List<Map<String, Object>> executeQuery(String query, Statement statement) throws SQLException {
        return extractFromResultSte(statement.executeQuery(query));
    }

    private List<Map<String, Object>> executeQuery(PreparedStatement statement) throws SQLException {
        return extractFromResultSte(statement.executeQuery());
    }

    private List<Map<String, Object>> extractFromResultSte(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();

        List<String> columns = extractColumns(resultSet);

        while (resultSet.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (String column : columns) {
                row.put(column, resultSet.getObject(column));
            }
            result.add(row);
        }
        return result;
    }

    private Object[] convertToRawParameters(Map<String, Object> searchCriteria) {
        return searchCriteria.values().stream()
                .map(this::getParameterValue)
                .toArray();
    }

    private String composeFindQuery(DatabaseRequest databaseRequest, Map<String, Object> searchCriteria) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT * FROM ");
        queryBuilder.append(databaseRequest.getTable());
        if (!searchCriteria.isEmpty()) {
            queryBuilder.append(" WHERE ");
            String columns = searchCriteria.entrySet().stream()
                    .map(entry -> {
                        if (entry.getValue() instanceof Map) {
                            String comparator = (String) ((Map) entry.getValue()).get(COMPARATOR);
                            return entry.getKey() + comparator + " ?";
                        } else {
                            return entry.getKey() + getCompareOperator(entry.getValue()) + "?";
                        }
                    })
                    .collect(Collectors.joining(" AND "));

            queryBuilder.append(columns);
        }
        return queryBuilder.toString();
    }

    private List<String> extractColumns(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metadata = resultSet.getMetaData();
        List<String> columns = new ArrayList<>();
        for (int i = 1; i <= metadata.getColumnCount(); i++) {
            columns.add(metadata.getColumnName(i));
        }
        return columns;
    }

    private String getCompareOperator(Object valueToCompare) {
        return String.valueOf(valueToCompare).contains("%") ? " like " : " = ";
    }
}
