package org.skellig.teststep.processing.valueextractor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class ConcatTestStepValueExtractorTest {

    private val extractor = ConcatTestStepValueExtractor()

    @Test
    fun testConcatString() {
        assertEquals("value_more data", extractor.extract("value", "_more data"))
    }

    @Test
    fun testConcatWhenNull() {
        assertNull(extractor.extract(null, "something"))
    }

    @Test
    fun testConcatWhenObject() {
        assertEquals("[a, b]something", extractor.extract(listOf("a","b"), "something"))
    }
}