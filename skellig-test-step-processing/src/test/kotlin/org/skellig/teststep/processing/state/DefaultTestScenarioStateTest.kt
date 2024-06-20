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

    @Test
    fun testRemove() {
        scenarioState.set("key1", "value1")
        scenarioState.remove("key1")

        assertNull(scenarioState.get("key1"))
    }

    @Test
    fun testClean() {
        scenarioState.set("key1", "value1")
        scenarioState.clean()

        assertNull(scenarioState.get("key1"))
    }

    @Test
    fun testIterator() {
        scenarioState.set("key1", "value1")
        scenarioState.set("key2", "value2")
        val iterator = scenarioState.iterator()

        assertAll(
            { assertTrue(iterator.hasNext()) },
            { assertEquals(Pair("key1", "value1"), iterator.next()) },
            { assertTrue(iterator.hasNext()) },
            { assertEquals(Pair("key2", "value2"), iterator.next()) },
            { assertFalse(iterator.hasNext()) }
        )
    }
}