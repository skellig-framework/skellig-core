package org.skellig.teststep.processor.cassandra;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skellig.teststep.processor.db.model.DatabaseRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CassandraSelectRequestExecutorTest {

    private CassandraSelectRequestExecutor executor;
    private Session session;

    @BeforeEach
    void setUp() {
        session = mock(Session.class);
        executor = new CassandraSelectRequestExecutor(session);
    }

    @Test
    void testFindUsingQuery() {
        DatabaseRequest databaseRequest = mock(DatabaseRequest.class);
        when(databaseRequest.getQuery()).thenReturn("select query");

        ResultSet resultSet = createResultSet();
        when(session.execute(databaseRequest.getQuery())).thenReturn(resultSet);

        List<Map> response = (List<Map>) executor.execute(databaseRequest);

        assertEquals(1, response.size());
    }

    @Test
    void testFindUsingCommandWithoutFilter() {
        String sql = "SELECT * FROM t1 ALLOW FILTERING";
        DatabaseRequest databaseRequest = new DatabaseRequest("select", "t1", null);

        ResultSet resultSet = createResultSet();
        when(session.execute(any(Statement.class)))
                .thenAnswer(o -> {
                    SimpleStatement statement = (SimpleStatement) o.getArguments()[0];
                    if (statement.getQueryString().equals(sql)) {
                        return resultSet;
                    } else {
                        return null;
                    }
                });

        List<Map> response = (List<Map>) executor.execute(databaseRequest);

        assertEquals(1, response.size());
    }

    @Test
    void testFindUsingCommandWithFilter() {
        String sql = "SELECT * FROM t1 WHERE c1 = ? AND c2 = ? ALLOW FILTERING";
        Map<String, Object> filter = new HashMap<>();
        filter.put("c1", "v1");
        filter.put("c2", 20);

        DatabaseRequest databaseRequest = new DatabaseRequest("select", "t1", filter);

        ResultSet resultSet = createResultSet();
        when(session.execute(any(Statement.class)))
                .thenAnswer(o -> {
                    SimpleStatement statement = (SimpleStatement) o.getArguments()[0];
                    if (statement.getQueryString().equals(sql)) {
                        return resultSet;
                    } else {
                        return null;
                    }
                });

        List<Map> response = (List<Map>) executor.execute(databaseRequest);

        assertEquals(1, response.size());
    }

    private ResultSet createResultSet() {
        Row row = mock(Row.class);
        when(row.getObject("c1")).thenReturn("v1");
        when(row.getObject("c2")).thenReturn(20);

        when(row.getColumnDefinitions()).thenReturn(mock(ColumnDefinitions.class));

        ResultSet resultSet = mock(ResultSet.class);
        doAnswer(o -> {
            ((Consumer) o.getArguments()[0]).accept(row);
            return o;
        }).when(resultSet).forEach(anyObject());

        return resultSet;
    }

}