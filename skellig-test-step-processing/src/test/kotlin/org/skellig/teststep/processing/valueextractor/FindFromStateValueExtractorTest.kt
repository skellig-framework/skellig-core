package org.skellig.teststep.processing.valueextractor

import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class FindFromStateValueExtractorTest {

    private val findFromStateValueExtractor = FindFromStateValueExtractor(ObjectTestStepValueExtractor())

    @Test
    fun testFindWithTwoSameValuesFromExtractionPaths() {
        val stateValue1 = mapOf(Pair("a", mapOf(Pair("b", mapOf(Pair("c", "v1"))))))
        val stateValue2 = mapOf(Pair("a", mapOf(Pair("b", mapOf(Pair("c", "v2"))))))
        val value = listOf(stateValue1, stateValue2, mock())

        assertEquals("v1", findFromStateValueExtractor.extractFrom("find", value, arrayOf("a", "b", "c")))
    }

    @Test
    fun testFindWithNothingFound() {
        val value = listOf(mapOf(Pair("a", "v1")))

        assertNull(findFromStateValueExtractor.extractFrom("find", value, arrayOf("b")))
    }

}