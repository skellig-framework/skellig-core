package org.skellig.teststep.processing.value.extractor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skellig.teststep.processing.exception.ValueExtractionException

class NumericOperatorValueExtractorTest {

    val converter = PlusOperatorTestStepValueExtractor()

    @Test
    fun testAdd() {
        val extractor = PlusOperatorTestStepValueExtractor()

        assertEquals(5, extractor.extractFrom("plus", "2", arrayOf("3")))
        assertEquals(5, extractor.extractFrom("plus", 2, arrayOf("3")))
        assertEquals(5.toShort(), extractor.extractFrom("plus", 2.toShort(), arrayOf("3")))
        assertEquals(5.toByte(), extractor.extractFrom("plus", 2.toByte(), arrayOf("3")))
        assertEquals(5.toLong(), extractor.extractFrom("plus", 2.toLong(), arrayOf("3")))
        assertEquals(5.toFloat(), extractor.extractFrom("plus", 2.toFloat(), arrayOf("3")))
        assertEquals((5.8).toFloat(), extractor.extractFrom("plus", (2.1).toFloat(), arrayOf("3.7")))
        assertEquals(5.toDouble(), extractor.extractFrom("plus", 2.toDouble(), arrayOf("3")))
        assertEquals(5.toBigDecimal(), extractor.extractFrom("plus", 2.toBigDecimal(), arrayOf("3")))
    }

    @Test
    fun testTimes() {
        val extractor = TimesOperatorTestStepValueExtractor()

        assertEquals(6, extractor.extractFrom("times", "2", arrayOf("3")))
        assertEquals(6, extractor.extractFrom("times", 2, arrayOf("3")))
        assertEquals(6.toShort(), extractor.extractFrom("times", 2.toShort(), arrayOf("3")))
        assertEquals(6.toByte(), extractor.extractFrom("times", 2.toByte(), arrayOf("3")))
        assertEquals(6.toLong(), extractor.extractFrom("times", 2.toLong(), arrayOf("3")))
        assertEquals(6.toFloat(), extractor.extractFrom("times", 2.toFloat(), arrayOf("3")))
        assertEquals(6.toDouble(), extractor.extractFrom("times", 2.toDouble(), arrayOf("3")))
        assertEquals(6.toBigDecimal(), extractor.extractFrom("times", 2.toBigDecimal(), arrayOf("3")))
    }

    @Test
    fun testDiv() {
        val extractor = DivOperatorTestStepValueExtractor()

        assertEquals(1, extractor.extractFrom("div", "3", arrayOf("2")))
        assertEquals(1, extractor.extractFrom("div", 3, arrayOf("2")))
        assertEquals(1.toShort(), extractor.extractFrom("div", 3.toShort(), arrayOf("2")))
        assertEquals(1.toByte(), extractor.extractFrom("div", 3.toByte(), arrayOf("2")))
        assertEquals(1.toLong(), extractor.extractFrom("div", 3.toLong(), arrayOf("2")))
        assertEquals(1.5.toFloat(), extractor.extractFrom("div", 3.toFloat(), arrayOf("2")))
        assertEquals(1.5, extractor.extractFrom("div", 3.toDouble(), arrayOf("2")))
        assertEquals(2.toBigDecimal(), extractor.extractFrom("div", 3.toBigDecimal(), arrayOf("2")))
    }

    @Test
    fun testMinus() {
        val extractor = MinusOperatorTestStepValueExtractor()

        assertEquals(-1, extractor.extractFrom("minus", "2", arrayOf("3")))
        assertEquals(-1, extractor.extractFrom("minus", 2, arrayOf("3")))
        assertEquals((-1).toShort(), extractor.extractFrom("minus", 2.toShort(), arrayOf("3")))
        assertEquals((-1).toByte(), extractor.extractFrom("minus", 2.toByte(), arrayOf("3")))
        assertEquals((-1).toLong(), extractor.extractFrom("minus", 2.toLong(), arrayOf("3")))
        assertEquals((-1).toFloat(), extractor.extractFrom("minus", 2.toFloat(), arrayOf("3")))
        assertEquals((-1).toDouble(), extractor.extractFrom("minus", 2.toDouble(), arrayOf("3")))
        assertEquals((-1).toBigDecimal(), extractor.extractFrom("minus", 2.toBigDecimal(), arrayOf("3")))
        assertEquals((-1.8).toBigDecimal(), extractor.extractFrom("minus", 2.toBigDecimal(), arrayOf("3.8")))
    }

    @Test
    fun testWitWrongType() {
        assertThrows<NumberFormatException> { MinusOperatorTestStepValueExtractor().extractFrom("add", 2.toBigDecimal(), arrayOf("gg")) }
        assertThrows<ValueExtractionException> { PlusOperatorTestStepValueExtractor().extractFrom("plus", 2, arrayOf("gg")) }
        assertThrows<ValueExtractionException> { MinusOperatorTestStepValueExtractor().extractFrom("minus", 2, arrayOf("gg")) }
        assertThrows<ValueExtractionException> { DivOperatorTestStepValueExtractor().extractFrom("div", 2, arrayOf("gg")) }
        assertThrows<ValueExtractionException> { TimesOperatorTestStepValueExtractor().extractFrom("times", 2, arrayOf("gg")) }
    }
}