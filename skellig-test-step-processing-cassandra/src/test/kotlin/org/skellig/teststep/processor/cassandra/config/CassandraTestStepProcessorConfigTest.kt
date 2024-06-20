package org.skellig.teststep.processor.cassandra.config

import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfigDetails

class CassandraTestStepProcessorConfigTest {

    @Test
    fun `configure Cassandra test step processor if no config provided`() {
        assertNull(
            CassandraTestStepProcessorConfig().config(
                TestStepProcessorConfigDetails(
                    mock(), mock(), mock(), mock(), mock(), mock()
                )
            )
        )
    }
}