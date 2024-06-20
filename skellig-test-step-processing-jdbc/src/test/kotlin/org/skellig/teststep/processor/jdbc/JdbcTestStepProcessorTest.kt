package org.skellig.teststep.processor.jdbc

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.skellig.teststep.processor.jdbc.model.JdbcTestStep

class JdbcTestStepProcessorTest {

    @Test
    fun `get test step class`() {
        assertEquals(JdbcTestStep::class.java,
            JdbcTestStepProcessor.Builder()
                .withTestScenarioState(mock())
                .build()
                .getTestStepClass())
    }
}