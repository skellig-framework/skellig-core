package org.skellig.teststep.processor.cassandra;

import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.skellig.teststep.processing.exception.TestStepProcessingException;
import org.skellig.teststep.processor.db.model.DatabaseRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CassandraInsertRequestExecutorTest {

    private CassandraInsertRequestExecutor executor;
    private Session session;

    @BeforeEach
    void setUp() {
        session = mock(Session.class);
        executor = new CassandraInsertRequestExecutor(session);
    }

    @Test
    void testInsertUsingQuery() {
        DatabaseRequest databaseRequest = mock(DatabaseRequest.class);
        when(databaseRequest.getQuery()).thenReturn("insert query");

        ResultSet response = mock(ResultSet.class);
        when(session.execute(databaseRequest.getQuery())).thenReturn(response);

        assertEquals(response, executor.execute(databaseRequest));
    }

    @Test
    void testInsertUsingCommand() {
        String sql = "insert INTO t1 (c3,c4,c1,c2) VALUES(?,?,?,?)";
        Map<String, Object> data = new HashMap<>();
        data.put("c1", "v1");
        data.put("c2", 2);
        data.put("c3", LocalDateTime.of(2020, 1, 1, 10, 10));
        data.put("c4", LocalDate.of(2020, 2, 2));

        DatabaseRequest databaseRequest = new DatabaseRequest("insert", "t1", data);

        ResultSet response = mock(ResultSet.class);
        when(session.execute(any(Statement.class))).thenReturn(response);

        Object actualResponse = executor.execute(databaseRequest);

        assertAll(
                () -> assertEquals(response, actualResponse),
                () -> verify(session).execute(argThat(new ArgumentMatcher<Statement>() {
                    @Override
                    public boolean matches(Object o) {
                        SimpleStatement statement = (SimpleStatement) o;
                        return statement.getQueryString().equals(sql) &&
                                statement.getValues(ProtocolVersion.V3, CodecRegistry.DEFAULT_INSTANCE).length == 4;
                    }
                }))
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