package org.skellig.teststep.processor.rmq

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processor.rmq.model.RmqConsumableTestStep

class RmqConsumableTestStepProcessorTest {

    private var processor: RmqConsumableTestStepProcessor? = null

    @BeforeEach
    fun setUp() {
        val rmqChannels = mapOf(
            Pair("local", mock(RmqChannel::class.java)),
        )
        processor = RmqConsumableTestStepProcessor(rmqChannels, mock(TestScenarioState::class.java))
    }

    @Test
    @DisplayName("Verify correct test step class linked to the processor")
    fun testGetTestStepClass() {
        assertEquals(RmqConsumableTestStep::class.java, processor!!.getTestStepClass())
    }
}