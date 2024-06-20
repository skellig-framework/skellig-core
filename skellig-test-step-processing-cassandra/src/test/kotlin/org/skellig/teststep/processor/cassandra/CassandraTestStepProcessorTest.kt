package org.skellig.teststep.processor.cassandra

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.skellig.teststep.processor.cassandra.model.CassandraTestStep

class CassandraTestStepProcessorTest {

    @Test
    fun `get test class`() {
        assertEquals(
            CassandraTestStep::class.java,
            CassandraTestStepProcessor.Builder()
                .withTestScenarioState(mock())
                .build()
                .getTestStepClass()
        )
    }
}