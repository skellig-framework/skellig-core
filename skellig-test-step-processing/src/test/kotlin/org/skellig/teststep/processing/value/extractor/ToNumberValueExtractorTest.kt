package org.skellig.teststep.processing.value.extractor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skellig.teststep.processing.exception.ValueExtractionException
import org.skellig.teststep.processing.value.extractor.ToBigDecimalTestStepValueExtractor
import org.skellig.teststep.processing.value.extractor.ToDoubleTestStepValueExtractor
import java.math.BigDecimal

class ToNumberValueExtractorTest {

    @Test
    fun testToNumber() {
        assertEquals(BigDecimal("100"), ToBigDecimalTestStepValueExtractor().extractFrom("toBigDecimal", "100", emptyArray()))
    }

    @Test
    fun testToDoubleFromInteger() {
        assertEquals(100.0, ToDoubleTestStepValueExtractor().extractFrom("toBigDecimal", 100, emptyArray()))
    }

    @Test
    fun testToNumberWhenNotString() {
        assertThrows<ValueExtractionException>("Failed to extract numeric value from type class kotlin.collections.EmptyList")
        { ToBigDecimalTestStepValueExtractor().extractFrom("toBigDecimal", listOf<String>(), emptyArray()) }
    }

    @Test
    fun testToNumberWhenNull() {
        assertThrows<ValueExtractionException>("Failed to extract numeric value from type null")
        { ToBigDecimalTestStepValueExtractor().extractFrom("toBigDecimal", null, emptyArray()) }
    }
}