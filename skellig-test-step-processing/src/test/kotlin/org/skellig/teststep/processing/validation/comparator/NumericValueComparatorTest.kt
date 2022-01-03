package org.skellig.teststep.processing.validation.comparator

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skellig.teststep.processing.exception.ValidationException
import java.math.BigDecimal

class NumericValueComparatorTest {

    private val comparator = NumericValueComparator()

    @Test
    fun testLessThanOrEqual() {
        assertTrue(comparator.compare("lessThan(15)", 10))
        assertTrue(comparator.compare("lessThan(0.1)", -1.01))
        assertTrue(comparator.compare("lessThan(0.05)", 0.0001))
        assertFalse(comparator.compare("lessThan(5)", 10))
        assertFalse(comparator.compare("lessThan(10)", 10))
        assertFalse(comparator.compare("lessThan(5)", BigDecimal(10)))

        assertTrue(comparator.compare("lessOrEqual(10)", 10))
        assertTrue(comparator.compare("lessOrEqual(5)", BigDecimal(5)))
    }

    @Test
    fun testMoreThanOrEqual() {
        assertFalse(comparator.compare("moreThan(15)", 10))
        assertTrue(comparator.compare("moreThan(5)", 10))
        assertTrue(comparator.compare("moreThan(-155)", 3))
        assertTrue(comparator.compare("moreThan(10.788888)", 10.788889))
        assertFalse(comparator.compare("moreThan(10)", 10))
        assertTrue(comparator.compare("moreThan(5)", BigDecimal(10)))

        assertTrue(comparator.compare("moreOrEqual(0)", 0))
        assertTrue(comparator.compare("moreOrEqual(10)", 10))
        assertTrue(comparator.compare("moreOrEqual(5)", BigDecimal(5)))
    }

    @Test
    fun testLessThanWhenActualIsString() {
        // string value must be ignored
        assertFalse(comparator.compare("lessThan(10)", "5"))
    }

    @Test
    fun testLessThanWhenNullOrEmptyActualValue() {
        assertFalse(comparator.compare("lessThan(10)", ""))
        assertFalse(comparator.compare("lessThan(10)", null))
    }

    @Test
    fun testLessThanWithInvalidFormat() {
        val ex = assertThrows<ValidationException> { comparator.compare("lessThan(abc)", 10) }

        assertEquals("Invalid number format in function 'lessThan': 'abc'", ex.message)

        assertThrows<ValidationException> { comparator.compare("moreThan(10_)", BigDecimal(1)) }
    }

    @Test
    fun testIsApplicable() {
        assertTrue(comparator.isApplicable("lessThan(10)"))
        assertTrue(comparator.isApplicable("moreThan(-9.21)"))
        assertTrue(comparator.isApplicable("lessOrEqual(invalid)"))
        assertTrue(comparator.isApplicable("moreOrEqual(0)"))
        assertFalse(comparator.isApplicable("_moreOrEqual(-9.21)"))
    }
}