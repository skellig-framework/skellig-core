package org.skellig.teststep.processing.valueextractor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skellig.teststep.processing.exception.ValueExtractionException

class NumericOperatorTestStepValueExtractorTest {

    val converter = PlusOperatorTestStepValueExtractor()

    @Test
    fun testAdd() {
        val extractor = PlusOperatorTestStepValueExtractor()

        assertEquals(5, extractor.extract("2", "3"))
        assertEquals(5, extractor.extract(2, "3"))
        assertEquals(5.toShort(), extractor.extract(2.toShort(), "3"))
        assertEquals(5.toByte(), extractor.extract(2.toByte(), "3"))
        assertEquals(5.toLong(), extractor.extract(2.toLong(), "3"))
        assertEquals(5.toFloat(), extractor.extract(2.toFloat(), "3"))
        assertEquals((5.8).toFloat(), extractor.extract((2.1).toFloat(), "3.7"))
        assertEquals(5.toDouble(), extractor.extract(2.toDouble(), "3"))
        assertEquals(5.toBigDecimal(), extractor.extract(2.toBigDecimal(), "3"))
    }

    @Test
    fun testTimes() {
        val extractor = TimesOperatorTestStepValueExtractor()

        assertEquals(6, extractor.extract("2", "3"))
        assertEquals(6, extractor.extract(2, "3"))
        assertEquals(6.toShort(), extractor.extract(2.toShort(), "3"))
        assertEquals(6.toByte(), extractor.extract(2.toByte(), "3"))
        assertEquals(6.toLong(), extractor.extract(2.toLong(), "3"))
        assertEquals(6.toFloat(), extractor.extract(2.toFloat(), "3"))
        assertEquals(6.toDouble(), extractor.extract(2.toDouble(), "3"))
        assertEquals(6.toBigDecimal(), extractor.extract(2.toBigDecimal(), "3"))
    }

    @Test
    fun testDiv() {
        val extractor = DivOperatorTestStepValueExtractor()

        assertEquals(1, extractor.extract("3", "2"))
        assertEquals(1, extractor.extract(3, "2"))
        assertEquals(1.toShort(), extractor.extract(3.toShort(), "2"))
        assertEquals(1.toByte(), extractor.extract(3.toByte(), "2"))
        assertEquals(1.toLong(), extractor.extract(3.toLong(), "2"))
        assertEquals(1.5.toFloat(), extractor.extract(3.toFloat(), "2"))
        assertEquals(1.5, extractor.extract(3.toDouble(), "2"))
        assertEquals(1.toBigDecimal(), extractor.extract(3.toBigDecimal(), "2"))
    }

    @Test
    fun testMinus() {
        val extractor = MinusOperatorTestStepValueExtractor()

        assertEquals(-1, extractor.extract("2", "3"))
        assertEquals(-1, extractor.extract(2, "3"))
        assertEquals((-1).toShort(), extractor.extract(2.toShort(), "3"))
        assertEquals((-1).toByte(), extractor.extract(2.toByte(), "3"))
        assertEquals((-1).toLong(), extractor.extract(2.toLong(), "3"))
        assertEquals((-1).toFloat(), extractor.extract(2.toFloat(), "3"))
        assertEquals((-1).toDouble(), extractor.extract(2.toDouble(), "3"))
        assertEquals((-1).toBigDecimal(), extractor.extract(2.toBigDecimal(), "3"))
        assertEquals((-1.8).toBigDecimal(), extractor.extract(2.toBigDecimal(), "3.8"))
    }

    @Test
    fun testWitWrongType() {
        assertThrows<NumberFormatException> { MinusOperatorTestStepValueExtractor().extract(2.toBigDecimal(), "gg") }
        assertThrows<ValueExtractionException> { PlusOperatorTestStepValueExtractor().extract(2, "gg") }
        assertThrows<ValueExtractionException> { MinusOperatorTestStepValueExtractor().extract(2, "gg") }
        assertThrows<ValueExtractionException> { DivOperatorTestStepValueExtractor().extract(2, "gg") }
        assertThrows<ValueExtractionException> { TimesOperatorTestStepValueExtractor().extract(2, "gg") }
    }
}