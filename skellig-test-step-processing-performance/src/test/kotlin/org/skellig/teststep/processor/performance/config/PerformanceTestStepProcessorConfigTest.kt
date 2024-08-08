package org.skellig.teststep.processor.performance.config

import com.typesafe.config.ConfigFactory
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfigDetails

class PerformanceTestStepProcessorConfigTest {

    @Test
    fun `config performance processor where config not defined`() {
        val config = PerformanceTestStepProcessorConfig().config(
            TestStepProcessorConfigDetails(mock(), ConfigFactory.load("empty-performance-test.conf"), mock(), mock(), mock(), mock())
        )
        assertNull(config, "Config should not be defined")
    }

}