package org.skellig.teststep.processor.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skellig.teststep.processing.exception.TestStepProcessingException;
import org.skellig.teststep.processor.db.model.DatabaseRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabaseRequestExecutorFactoryTest {

    private DatabaseRequestExecutorFactory factory;
    private DatabaseRequestExecutor selectExecutor;
    private DatabaseRequestExecutor insertExecutor;

    @BeforeEach
    void setUp() {
        selectExecutor = mock(DatabaseRequestExecutor.class);
        insertExecutor = mock(DatabaseRequestExecutor.class);

        factory = new DatabaseRequestExecutorFactory(selectExecutor, insertExecutor);
    }

    @Test
    void testGetRequestExecutorForSelectCommand() {
        DatabaseRequest databaseRequest = mock(DatabaseRequest.class);
        when(databaseRequest.getCommand()).thenReturn("select");

        DatabaseRequestExecutor result = factory.get(databaseRequest);

        assertEquals(selectExecutor, result);
    }

    @Test
    void testGetRequestExecutorForInsertCommand() {
        DatabaseRequest databaseRequest = mock(DatabaseRequest.class);
        when(databaseRequest.getCommand()).thenReturn("insert");

        DatabaseRequestExecutor result = factory.get(databaseRequest);

        assertEquals(insertExecutor, result);
    }

    @Test
    void testGetRequestExecutorForInsertQuery() {
        DatabaseRequest databaseRequest = mock(DatabaseRequest.class);
        when(databaseRequest.getQuery()).thenReturn(" insert into V(select, gg, bb)");

        DatabaseRequestExecutor result = factory.get(databaseRequest);

        assertEquals(insertExecutor, result);
    }

    @Test
    void testGetRequestExecutorForSelectQuery() {
        DatabaseRequest databaseRequest = mock(DatabaseRequest.class);
        when(databaseRequest.getQuery()).thenReturn(" select * from V when a = insert");

        DatabaseRequestExecutor result = factory.get(databaseRequest);

        assertEquals(selectExecutor, result);
    }

    @Test
    void testGetRequestExecutorForUndefinedQuery() {
        DatabaseRequest databaseRequest = mock(DatabaseRequest.class);
        when(databaseRequest.getQuery()).thenReturn("some query");

        TestStepProcessingException ex =
                assertThrows(TestStepProcessingException.class, () -> factory.get(databaseRequest));

        assertEquals("No database query executors found for query: 'some query'." +
                " Supported types of queries: [select, insert]", ex.getMessage());
    }

    @Test
    void testGetRequestExecutorForUndefinedCommand() {
        DatabaseRequest databaseRequest = mock(DatabaseRequest.class);
        when(databaseRequest.getCommand()).thenReturn("wrong command");

        TestStepProcessingException ex =
                assertThrows(TestStepProcessingException.class, () -> factory.get(databaseRequest));

        assertEquals("No database query executors found for command: 'wrong command'." +
                " Supported commands: [select, insert]", ex.getMessage());
    }
}