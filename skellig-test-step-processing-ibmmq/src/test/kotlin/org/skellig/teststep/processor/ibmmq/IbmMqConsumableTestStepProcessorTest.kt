package org.skellig.teststep.processor.ibmmq

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.skellig.teststep.processor.ibmmq.model.IbmMqConsumableTestStep

class IbmMqConsumableTestStepProcessorTest {

    @Test
    @DisplayName("Verify correct test step class")
    fun testGetTestStepClass() {
        assertEquals(IbmMqConsumableTestStep::class.java, IbmMqConsumableTestStepProcessor(emptyMap(), mock()).getTestStepClass())
    }
}