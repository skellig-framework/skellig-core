package org.skellig.teststep.processing.converter

import com.nhaarman.mockitokotlin2.mock
import org.junit.Ignore
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.valueextractor.DefaultValueExtractor

@Ignore
class FindFromStateValueConverterTest {

    /*private val testScenarioState = DefaultTestScenarioState()
    private val converter = FindFromStateValueConverter(testScenarioState, DefaultValueExtractor.Builder().build())

    @Test
    fun testFind() {
        val stateValue1 = mapOf(Pair("a", mapOf(Pair("b", mapOf(Pair("c", "v1"))))))
        val stateValue2 = mapOf(Pair("a", mapOf(Pair("b", mapOf(Pair("c", "v2"))))))
        testScenarioState.set("result1", stateValue1)
        testScenarioState.set("result2", stateValue2)
        testScenarioState.set("result3", mock())

        assertEquals("v2", converter.convert("find(a.b.c)"))
    }

    @Test
    fun testFindWithDifferentExtractors() {
        val stateValue1 = mapOf(Pair("a", mapOf(Pair("b", "c/v2"))))
        testScenarioState.set("result1", stateValue1)

        assertEquals("v2", converter.convert("find(a.b.subStringLast(/))"))
    }

    @Test
    fun testFindWithPrefixAndSuffix() {
        val stateValue1 = mapOf(Pair("a", mapOf(Pair("b", mapOf(Pair("c", "v1"))))))
        testScenarioState.set("result1", stateValue1)

        assertEquals("prefix_v1-v1_suffix", converter.convert("prefix_find(a.b.c)-find(a.b.c)_suffix"))
    }

    @Test
    fun testFindWithJsonExtractor() {
        val stateValue1 = mapOf(Pair("a", mapOf(Pair("body",
                                                     """{ "a":{ "b":"true"} }"""))))
        testScenarioState.set("result1", stateValue1)

        assertEquals("true", converter.convert("find(a.body.jsonPath(a.b))"))
    }*/
}