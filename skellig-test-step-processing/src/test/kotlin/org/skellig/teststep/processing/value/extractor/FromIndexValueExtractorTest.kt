package org.skellig.teststep.processing.value.extractor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.exception.TestDataConversionException
import org.skellig.teststep.processing.exception.ValueExtractionException
import org.skellig.teststep.processing.value.extractor.FromIndexValueExtractor

class FromIndexValueExtractorTest {

    private val extractor = FromIndexValueExtractor()

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

    @Test
    fun testFromIndexWithInvalidNumberOfArguments() {
        val ex = assertThrows(ValueExtractionException::class.java) { extractor.extractFrom("fromIndex", listOf(1), arrayOf(0, 0)) }

        assertEquals("fromIndex function can accept only 1 argument. Found: 2", ex.message)
    }

    @Test
    fun testFromIndexOfNullValue() {
        val ex = assertThrows(ValueExtractionException::class.java) { extractor.extractFrom("fromIndex", null, arrayOf(0)) }

        assertEquals("Cannot extract '0' from null value", ex.message)
    }
}