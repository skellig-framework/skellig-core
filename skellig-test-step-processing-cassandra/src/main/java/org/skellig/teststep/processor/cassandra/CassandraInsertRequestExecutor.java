package org.skellig.teststep.processor.cassandra;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import org.skellig.teststep.processing.exception.TestStepProcessingException;
import org.skellig.teststep.processor.db.model.DatabaseRequest;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

class CassandraInsertRequestExecutor extends BaseCassandraRequestExecutor {

    private Session session;

    CassandraInsertRequestExecutor(Session session) {
        this.session = session;
    }

    @Override
    public Object execute(DatabaseRequest databaseRequest) {
        int result;
        try {
            if (databaseRequest.getQuery() != null) {
                return session.execute(databaseRequest.getQuery());
            } else {
                Map<String, Object> searchCriteria = databaseRequest.getColumnValuePairs().orElse(Collections.emptyMap());
                String query = composeInsertQuery(databaseRequest, searchCriteria);

                Object[] rawParameters = convertToRawParameters(searchCriteria);
                Statement preparedStatement = new SimpleStatement(query, rawParameters);

                session.execute(preparedStatement);
                result = 1;
            }
        } catch (Exception ex) {
            throw new TestStepProcessingException(ex.getMessage(), ex);
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
