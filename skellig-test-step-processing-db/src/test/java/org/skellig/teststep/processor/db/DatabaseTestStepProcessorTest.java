package org.skellig.teststep.processor.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.skellig.connection.database.DatabaseRequestExecutor;
import org.skellig.connection.database.model.DatabaseRequest;
import org.skellig.teststep.processing.converter.TestStepResultConverter;
import org.skellig.teststep.processing.exception.TestStepProcessingException;
import org.skellig.teststep.processing.state.DefaultTestScenarioState;
import org.skellig.teststep.processing.validation.DefaultTestStepResultValidator;
import org.skellig.teststep.processor.db.model.DatabaseTestStep;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Process database test step")
class DatabaseTestStepProcessorTest {

    private static final String SRV_2 = "srv2";
    private static final String SRV_1 = "srv1";

    private DatabaseTestStepProcessor databaseTestStepProcessor;
    private DatabaseRequestExecutor dbRequestExecutor1;
    private DatabaseRequestExecutor dbRequestExecutor2;
    private TestStepResultConverter testStepResultConverter;

    @BeforeEach
    void setUp() {
        dbRequestExecutor1 = mock(DatabaseRequestExecutor.class);
        dbRequestExecutor2 = mock(DatabaseRequestExecutor.class);
        testStepResultConverter = mock(TestStepResultConverter.class);

        Map<String, DatabaseRequestExecutor> dbChannels = new HashMap<>();
        dbChannels.put("srv1", dbRequestExecutor1);
        dbChannels.put("srv2", dbRequestExecutor2);

        databaseTestStepProcessor = new DatabaseTestStepProcessor(
                dbChannels, new DefaultTestScenarioState(),
                new DefaultTestStepResultValidator.Builder()
                        .build(),
                testStepResultConverter
        );
    }

    @Test
    @DisplayName("When no channel is registered Then throw exception")
    void testProcessDatabaseTestStepWhenNoChannelFound() {
        Object responseFromDb = new Object();
        when(dbRequestExecutor1.execute(argThat(new ArgumentMatcher<DatabaseRequest>() {
            @Override
            public boolean matches(Object o) {
                return false;
            }
        }))).thenReturn(responseFromDb);

        DatabaseTestStep testStep =
                new DatabaseTestStep.Builder()
                        .withServers(Collections.singletonList("default"))
                        .build();

        TestStepProcessingException ex = assertThrows(TestStepProcessingException.class,
                () -> databaseTestStepProcessor.processTestStep(testStep));

        assertEquals("No database channel was registered for server name 'default'", ex.getMessage());
    }

    @Test
    @DisplayName("When run only on one db server Then verify single response returned")
    void testProcessDatabaseTestStepForSingleChannel() {
        DatabaseTestStep testStep =
                new DatabaseTestStep.Builder()
                        .withServers(Collections.singletonList(SRV_1))
                        .withCommand("select")
                        .withTable("t1")
                        .build();

        Object responseFromDb = new Object();
        when(dbRequestExecutor1.execute(argThat(new ArgumentMatcher<DatabaseRequest>() {
            @Override
            public boolean matches(Object request) {
                DatabaseRequest databaseRequest = (DatabaseRequest) request;
                return databaseRequest.getCommand().equals(testStep.getCommand()) &&
                        databaseRequest.getTable().equals(testStep.getTable());
            }
        }))).thenReturn(responseFromDb);


        assertEquals(responseFromDb, databaseTestStepProcessor.processTestStep(testStep));
    }

    @Test
    @DisplayName("When run on 2 db servers And only query provided Then verify grouped response returned")
    void testProcessDatabaseTestStepForTwoChannelsWhenQueryProvided() {
        DatabaseTestStep testStep =
                new DatabaseTestStep.Builder()
                        .withServers(Arrays.asList(SRV_1, SRV_2))
                        .withQuery("select * from t1")
                        .build();

        // return results from all 2 db servers
        Object responseFromDb1 = new Object();
        when(dbRequestExecutor1.execute(argThat(new ArgumentMatcher<DatabaseRequest>() {
            @Override
            public boolean matches(Object request) {
                DatabaseRequest databaseRequest = (DatabaseRequest) request;
                return databaseRequest.getQuery().equals(testStep.getQuery());
            }
        }))).thenReturn(responseFromDb1);

        Object responseFromDb2 = new Object();
        when(dbRequestExecutor2.execute(argThat(new ArgumentMatcher<DatabaseRequest>() {
            @Override
            public boolean matches(Object request) {
                DatabaseRequest databaseRequest = (DatabaseRequest) request;
                return databaseRequest.getQuery().equals(testStep.getQuery());
            }
        }))).thenReturn(responseFromDb2);

        assertEquals(responseFromDb1, ((Map) databaseTestStepProcessor.processTestStep(testStep)).get(SRV_1));
        assertEquals(responseFromDb2, ((Map) databaseTestStepProcessor.processTestStep(testStep)).get(SRV_2));
    }

}