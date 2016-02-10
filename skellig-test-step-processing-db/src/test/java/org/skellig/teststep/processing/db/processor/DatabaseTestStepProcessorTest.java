package org.skellig.teststep.processing.db.processor;

import org.junit.jupiter.api.BeforeEach;
import org.skellig.teststep.processing.state.ThreadLocalTestScenarioState;
import org.skellig.teststep.processing.validation.DefaultTestStepResultValidator;

class DatabaseTestStepProcessorTest {

    private DatabaseTestStepProcessor databaseTestStepProcessor;

    @BeforeEach
    void setUp() {
        databaseTestStepProcessor = new DatabaseTestStepProcessor(
                dbServers, new ThreadLocalTestScenarioState(),
                new DefaultTestStepResultValidator.Builder()
                        .build()
        );
    }

}