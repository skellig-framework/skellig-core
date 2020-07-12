package org.skellig.connection.cassandra;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import org.skellig.connection.database.BaseDatabaseRequestExecutor;
import org.skellig.connection.database.exception.DatabaseChannelException;
import org.skellig.connection.database.model.DatabaseRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class CassandraSelectRequestExecutor extends BaseDatabaseRequestExecutor {

    private static final String COMPARATOR = "comparator";

    private Session session;

    public CassandraSelectRequestExecutor(Session session) {
        this.session = session;
    }

    @Override
    public Object execute(DatabaseRequest databaseRequest) {
        try {
            if (databaseRequest.getQuery() != null) {
                return session.execute(databaseRequest.getQuery());
            } else {
                Map<String, Object> searchCriteria = databaseRequest.getColumnValuePairs().orElse(Collections.emptyMap());
                String query = composeFindQuery(databaseRequest, searchCriteria);

                PreparedStatement preparedStatement = session.prepare(query);
                Object[] rawParameters = convertToRawParameters(searchCriteria);

                return extractFromResultSet(session.execute(preparedStatement.bind(rawParameters)));
            }

        } catch (Exception ex) {
            throw new DatabaseChannelException(ex.getMessage(), ex);
        }
    }

    private List<Map<String, Object>> extractFromResultSet(ResultSet resultSet) {
        List<Map<String, Object>> result = new ArrayList<>();

        resultSet.forEach(row -> {
            Map<String, Object> resultRow = new LinkedHashMap<>();
            result.add(resultRow);
            row.getColumnDefinitions().asList()
                    .forEach(column -> resultRow.put(column.getName(), row.getObject(column.getName())));
        });
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
                            String comparator = String.valueOf(((Map) entry.getValue()).get(COMPARATOR));
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

    private String getCompareOperator(Object valueToCompare) {
        return String.valueOf(valueToCompare).contains("%") ? " like " : " = ";
    }
}
