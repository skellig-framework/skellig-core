package org.skellig.teststep.processing.validation.comparator

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ContainsValueComparatorTest {

    private var containsValueComparator: ContainsValueComparator? = null

    @BeforeEach
    fun setUp() {
        containsValueComparator = ContainsValueComparator()
    }

    @Test
    fun testContainsInString() {
        Assertions.assertTrue(containsValueComparator!!.compare("contains", arrayOf("value 1"), "test value 1"))
    }

    @Test
    fun testNotContainsInString() {
        Assertions.assertFalse(containsValueComparator!!.compare("contains", arrayOf("boo"), "test value 1"))
    }

    @Test
    fun testContainsWhenActualIsNull() {
        Assertions.assertFalse(containsValueComparator!!.compare("contains", arrayOf("value 1"), null))
    }

    @Test
    fun testContainsWhenActualIsObject() {
        Assertions.assertFalse(containsValueComparator!!.compare("contains", arrayOf("value 1"), Any()))
    }

    @Test
    fun testContainsWhenActualIsArray() {
        Assertions.assertTrue(containsValueComparator!!.compare("contains", arrayOf("v1"), arrayOf("v1", "v2")))
    }

    @Test
    fun testContainsWhenActualIsArrayOfInteger() {
        Assertions.assertTrue(containsValueComparator!!.compare("contains", arrayOf("2"), arrayOf(1, 2, 3)))
    }
}