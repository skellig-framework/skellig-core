package org.skellig.teststep.processing.validation.comparator

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skellig.teststep.processing.exception.ValidationException
import java.math.BigDecimal

class NumericValueComparatorTest {

    @Test
    fun testLessThanOrEqual() {
        val comparator = LessThanValueComparator()

        assertTrue(comparator.compare ("lessThan", arrayOf("15"), 10))
        assertTrue(comparator.compare ("lessThan", arrayOf(" 0.1 "), -1.01))
        assertTrue(comparator.compare ("lessThan", arrayOf("0.05"), 0.0001))
        assertFalse(comparator.compare("lessThan", arrayOf("5"), 10))
        assertFalse(comparator.compare("lessThan", arrayOf("10"), 10))
        assertFalse(comparator.compare("lessThan", arrayOf("5"), BigDecimal(10)))

        val lessOrEqualComparator = LessOrEqualsValueComparator()
        assertTrue(lessOrEqualComparator.compare("lessOrEqual", arrayOf(10), 10))
        assertTrue(lessOrEqualComparator.compare("lessOrEqual", arrayOf(5), BigDecimal(5)))
    }

    @Test
    fun testMoreThanOrEqual() {
        val comparator = MoreThanValueComparator()

        assertFalse(comparator.compare("moreThan", arrayOf("15"), 10))
        assertTrue(comparator.compare ("moreThan", arrayOf("5 "), 10))
        assertTrue(comparator.compare ("moreThan", arrayOf(" -155 "), 3))
        assertTrue(comparator.compare ("moreThan", arrayOf("10.788888"), 10.788889))
        assertFalse(comparator.compare("moreThan", arrayOf("10"), 10))
        assertTrue(comparator.compare ("moreThan", arrayOf("5"), BigDecimal(10)))

        val moreOrEqualComparator = MoreOrEqualsThanComparator()
        assertTrue(moreOrEqualComparator.compare("moreOrEqual", arrayOf(0), 0))
        assertTrue(moreOrEqualComparator.compare("moreOrEqual", arrayOf(10), 10))
        assertTrue(moreOrEqualComparator.compare("moreOrEqual", arrayOf(5), BigDecimal(5)))
    }

    @Test
    fun testLessThanWhenActualIsString() {
        val comparator = MoreThanValueComparator()
        // string value must be ignored
        assertFalse(comparator.compare("lessThan", arrayOf(10), "5"))
    }

    @Test
    fun testLessThanWhenNullOrEmptyActualValue() {
        val comparator = LessThanValueComparator()

        assertFalse(comparator.compare("lessThan", arrayOf(10), ""))
        assertFalse(comparator.compare("lessThan", arrayOf(10), null))
    }

    @Test
    fun testLessThanWithInvalidFormat() {
        val comparator = LessThanValueComparator()

        val ex = assertThrows<ValidationException> { comparator.compare("lessThan", arrayOf("abc"), 10) }

        assertEquals("Invalid number format in function 'lessThan': 'abc'", ex.message)

        assertThrows<ValidationException> { comparator.compare("moreThan", arrayOf("10_"), BigDecimal(1)) }
    }
}