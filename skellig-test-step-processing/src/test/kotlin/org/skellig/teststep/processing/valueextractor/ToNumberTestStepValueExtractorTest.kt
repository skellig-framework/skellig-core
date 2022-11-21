package org.skellig.teststep.processing.valueextractor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skellig.teststep.processing.exception.ValueExtractionException
import java.math.BigDecimal

class ToNumberTestStepValueExtractorTest {

    @Test
    fun testToNumber() {
        assertEquals(BigDecimal("100"), ToBigDecimalTestStepValueExtractor().extractFrom("toBigDecimal", "100", arrayOf(null)))
    }

    @Test
    fun testToDoubleFromInteger() {
        assertEquals(100.0, ToDoubleTestStepValueExtractor().extractFrom("toBigDecimal", 100, arrayOf(null)))
    }

    @Test
    fun testToNumberWhenNotString() {
        assertThrows<ValueExtractionException>("Failed to extract numeric value from type class kotlin.collections.EmptyList")
        { ToBigDecimalTestStepValueExtractor().extractFrom("toBigDecimal", listOf<String>(), arrayOf(null)) }
    }

    @Test
    fun testToNumberWhenNull() {
        assertThrows<ValueExtractionException>("Failed to extract numeric value from type null")
        { ToBigDecimalTestStepValueExtractor().extractFrom("toBigDecimal", null, arrayOf(null)) }
    }
}