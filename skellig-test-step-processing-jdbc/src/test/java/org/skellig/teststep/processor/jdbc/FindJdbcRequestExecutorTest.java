package org.skellig.teststep.processor.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.skellig.teststep.processor.db.model.DatabaseRequest;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FindJdbcRequestExecutorTest {

    private FindJdbcRequestExecutor executor;
    private Connection connection;

    @BeforeEach
    void setUp() {
        connection = mock(Connection.class);
        executor = new FindJdbcRequestExecutor(connection);
    }

    @Test
    void testFindUsingQuery() throws SQLException {
        DatabaseRequest databaseRequest = mock(DatabaseRequest.class);
        when(databaseRequest.getQuery()).thenReturn("select query");

        ResultSet resultSet = createResultSet();
        Statement statement = mock(Statement.class);
        when(statement.executeQuery(databaseRequest.getQuery())).thenReturn(resultSet);
        when(connection.createStatement()).thenReturn(statement);

        List<Map> response = (List<Map>) executor.execute(databaseRequest);

        assertAll(
                () -> assertEquals(1, response.size()),
                () -> assertEquals(2, response.get(0).size()),
                () -> assertEquals("v1", response.get(0).get("c1")),
                () -> assertEquals("v2", response.get(0).get("c2"))
        );
    }

    @Test
    void testFindUsingCommandWithoutFilter() throws SQLException {
        String sql = "SELECT * FROM t1";
        DatabaseRequest databaseRequest = new DatabaseRequest("select", "t1", null);

        ResultSet resultSet = createResultSet();
        PreparedStatement statement = mock(PreparedStatement.class);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(connection.prepareStatement(sql)).thenReturn(statement);

        List<Map> response = (List<Map>) executor.execute(databaseRequest);

        assertEquals(1, response.size());
    }

    @Test
    void testFindUsingCommandWithFilter() throws SQLException {
        String sql = "SELECT * FROM t1 WHERE c3 like ? AND c4 in (?) AND c5 = ? AND c6 = ? AND c1 = ? AND c2 > ?";
        Map<String, Object> filter = new HashMap<>();
        filter.put("c1", "v1");
        filter.put("c2", new HashMap<String, Object>() {
            {
                put("comparator", ">");
                put("value", 10);
            }
        });
        filter.put("c3", new HashMap<String, String>() {
            {
                put("comparator", "like");
                put("value", "%a%");
            }
        });
        filter.put("c4", new HashMap<String, Object>() {
            {
                put("comparator", "in");
                put("value", Arrays.asList("a", "b"));
            }
        });
        filter.put("c5", LocalDateTime.of(2016, 1, 1, 10, 10));
        filter.put("c6", LocalDate.of(2016, 2, 2));

        DatabaseRequest databaseRequest = new DatabaseRequest("select", "t1", filter);

        ResultSet resultSet = createResultSet();
        PreparedStatement statement = mock(PreparedStatement.class);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(connection.prepareStatement(sql)).thenReturn(statement);

        List<Map> response = (List<Map>) executor.execute(databaseRequest);

        assertAll(
                () -> assertEquals(1, response.size()),
                () -> verify(statement).setObject(1, "%a%"),
                () -> verify(statement).setObject(eq(2), argThat(new ArgumentMatcher<Object>() {
                    @Override
                    public boolean matches(Object o) {
                        return ((List) o).contains("a") && ((List) o).contains("b");
                    }
                })),
                () -> verify(statement).setObject(eq(3), argThat(new ArgumentMatcher<Object>() {
                    @Override
                    public boolean matches(Object o) {
                        return o instanceof Timestamp &&
                                ((Timestamp) o).compareTo(Timestamp.valueOf((LocalDateTime) filter.get("c5"))) == 0;
                    }
                })),
                () -> verify(statement).setObject(eq(4), argThat(new ArgumentMatcher<Object>() {
                    @Override
                    public boolean matches(Object o) {
                        return o instanceof Date &&
                                ((Date) o).compareTo(Date.valueOf((LocalDate) filter.get("c6"))) == 0;
                    }
                })),
                () -> verify(statement).setObject(5, "v1"),
                () -> verify(statement).setObject(6, 10)
        );
    }

    private ResultSet createResultSet() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);

        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(metaData.getColumnCount()).thenReturn(2);
        when(metaData.getColumnName(1)).thenReturn("c1");
        when(metaData.getColumnName(2)).thenReturn("c2");

        when(resultSet.getMetaData()).thenReturn(metaData);
        when(resultSet.getObject("c1")).thenReturn("v1");
        when(resultSet.getObject("c2")).thenReturn("v2");

        // has only 1 row
        AtomicInteger counter = new AtomicInteger(0);
        when(resultSet.next()).thenAnswer(i -> counter.incrementAndGet() <= 1);

        return resultSet;
    }
}