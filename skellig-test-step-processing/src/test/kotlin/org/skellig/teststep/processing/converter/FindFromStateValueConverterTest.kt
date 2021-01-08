package org.skellig.teststep.processing.converter

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.valueextractor.TestStepValueExtractor

internal class FindFromStateValueConverterTest {

    private val testScenarioState = DefaultTestScenarioState()
    private val valueExtractor = mock<TestStepValueExtractor>()
    private val converter = FindFromStateValueConverter(testScenarioState, valueExtractor)

    @Test
    internal fun testFind() {
        val stateValue1 = mock<Any>()
        val stateValue2 = mock<Any>()
        testScenarioState.set("result1", stateValue1)
        testScenarioState.set("result2", stateValue2)
        testScenarioState.set("result3", mock())

        whenever(valueExtractor.extract(stateValue1, "a.b.c")).thenReturn("v1")
        whenever(valueExtractor.extract(stateValue2, "a.b.c")).thenReturn("v2")

        assertEquals("v2", converter.convert("find(a.b.c)"))
    }
}