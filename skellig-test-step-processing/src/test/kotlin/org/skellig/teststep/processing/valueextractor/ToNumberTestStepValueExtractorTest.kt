package org.skellig.teststep.processing.valueextractor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skellig.teststep.processing.exception.ValueExtractionException
import java.math.BigDecimal

class ToNumberTestStepValueExtractorTest {

    @Test
    fun testToNumber() {
        assertEquals(BigDecimal("100"), ToBigDecimalTestStepValueExtractor().extract("100", null));
    }

    @Test
    fun testToDoubleFromInteger() {
        assertEquals(100.0, ToDoubleTestStepValueExtractor().extract(100, null));
    }

    @Test
    fun testToNumberWhenNotString() {
        assertThrows<ValueExtractionException>("Failed to extract numeric value from type class kotlin.collections.EmptyList")
        { ToBigDecimalTestStepValueExtractor().extract(listOf<String>(), null) }
    }

    @Test
    fun testToNumberWhenNull() {
        assertThrows<ValueExtractionException>("Failed to extract numeric value from type null")
        { ToBigDecimalTestStepValueExtractor().extract(null, null) }
    }
}