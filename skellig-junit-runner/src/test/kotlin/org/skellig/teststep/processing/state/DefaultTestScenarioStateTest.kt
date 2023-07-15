package org.skellig.teststep.processing.state

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DefaultTestScenarioStateTest {

    private val scenarioState = DefaultTestScenarioState()

    @Test
    fun testSet() {
        scenarioState.set("key1", "value1")

        assertEquals("value1", scenarioState.get("key1"))
    }
}