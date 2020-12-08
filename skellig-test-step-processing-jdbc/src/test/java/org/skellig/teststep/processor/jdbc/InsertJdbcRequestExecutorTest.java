package org.skellig.teststep.processor.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.skellig.teststep.processing.exception.TestStepProcessingException;
import org.skellig.teststep.processor.db.model.DatabaseRequest;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InsertJdbcRequestExecutorTest {

    private InsertJdbcRequestExecutor executor;
    private Connection connection;

    @BeforeEach
    void setUp() {
        connection = mock(Connection.class);
        executor = new InsertJdbcRequestExecutor(connection);
    }

    @Test
    void testInsertUsingQuery() throws SQLException {
        DatabaseRequest databaseRequest = mock(DatabaseRequest.class);
        when(databaseRequest.getQuery()).thenReturn("insert query");

        Statement statement = mock(Statement.class);
        when(statement.executeUpdate(databaseRequest.getQuery())).thenReturn(1);
        when(connection.createStatement()).thenReturn(statement);

        assertEquals(1, executor.execute(databaseRequest));
    }

    @Test
    void testInsertUsingCommand() throws SQLException {
        String sql = "insert INTO t1 (c3,c4,c1,c2) VALUES(?,?,?,?)";
        Map<String, Object> data = new HashMap<>();
        data.put("c1", "v1");
        data.put("c2", 2);
        data.put("c3", LocalDateTime.of(2020, 1, 1, 10, 10));
        data.put("c4", LocalDate.of(2020, 2, 2));

        DatabaseRequest databaseRequest = new DatabaseRequest("insert", "t1", data);

        PreparedStatement statement = mock(PreparedStatement.class);
        when(statement.executeUpdate()).thenReturn(1);
        when(connection.prepareStatement(sql)).thenReturn(statement);

        assertAll(
                () -> assertEquals(1, executor.execute(databaseRequest)),
                () -> verify(statement).setObject(eq(1), argThat(new ArgumentMatcher<Object>() {
                    @Override
                    public boolean matches(Object o) {
                        return o instanceof Timestamp &&
                                ((Timestamp) o).compareTo(Timestamp.valueOf((LocalDateTime) data.get("c3"))) == 0;
                    }
                })),
                () -> verify(statement).setObject(eq(2), argThat(new ArgumentMatcher<Object>() {
                    @Override
                    public boolean matches(Object o) {
                        return o instanceof Date &&
                                ((Date) o).compareTo(Date.valueOf((LocalDate) data.get("c4"))) == 0;
                    }
                })),
                () -> verify(statement).setObject(3, "v1"),
                () -> verify(statement).setObject(4, 2)
        );
    }

    @Test
    void testInsertUsingCommandWithoutData() {
        DatabaseRequest databaseRequest = new DatabaseRequest("insert", "t1", null);

        TestStepProcessingException ex = assertThrows(TestStepProcessingException.class,
                () -> executor.execute(databaseRequest));

        assertEquals("Cannot insert empty data to table t1", ex.getMessage());
    }
}