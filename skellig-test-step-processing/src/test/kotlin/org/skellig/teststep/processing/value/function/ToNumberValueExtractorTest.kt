package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import java.math.BigDecimal

class ToNumberValueExtractorTest {

    @Test
    fun testToNumber() {
        assertEquals(BigDecimal("100"), ToBigDecimalTestStepValueExtractor().execute("toBigDecimal", "100", emptyArray()))
    }

    @Test
    fun testToDoubleFromInteger() {
        assertEquals(100.0, ToDoubleTestStepValueExtractor().execute("toBigDecimal", 100, emptyArray()))
    }

    @Test
    fun testToNumberWhenNotString() {
        assertThrows<FunctionExecutionException>("Failed to extract numeric value from type class kotlin.collections.EmptyList")
        { ToBigDecimalTestStepValueExtractor().execute("toBigDecimal", listOf<String>(), emptyArray()) }
    }

    @Test
    fun testToNumberWhenNull() {
        assertThrows<FunctionExecutionException>("Failed to extract numeric value from type null")
        { ToBigDecimalTestStepValueExtractor().execute("toBigDecimal", null, emptyArray()) }
    }
}