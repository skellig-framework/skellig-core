package org.skellig.teststep.processing.valueextractor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FromIndexTestStepValueExtractorTest {

    private val extractor = FromIndexTestStepValueExtractor()

    @Test
    fun testFromIndexOfArray() {
        val value = arrayOf(1, 2, 3)

        assertEquals(2, extractor.extractFrom("fromIndex", value, arrayOf(1)))
        assertEquals(3, extractor.extractFrom("fromIndex", value, arrayOf("2")))
    }

    @Test
    fun testFromIndexOfList() {
        val value = listOf(1, 2, 3)

        assertEquals(3, extractor.extractFrom("fromIndex", value, arrayOf("2")))
    }
}