package org.skellig.teststep.processing.validation.comparator

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.value.function.ContainsValueComparator

class ContainsValueComparatorTest {

    private var containsValueComparator = ContainsValueComparator()

    @Test
    fun testContainsInString() {
        assertTrue(containsValueComparator.execute("contains", "test value 1", arrayOf("value 1")) as Boolean)
    }

    @Test
    fun testNotContainsInString() {
        assertFalse(containsValueComparator.execute("contains", "test value 1", arrayOf("boo")) as Boolean)
    }

    @Test
    fun testContainsWhenActualIsNull() {
        assertFalse(containsValueComparator.execute("contains", null, arrayOf("value 1")) as Boolean)
    }

    @Test
    fun testContainsWhenActualIsObject() {
        assertFalse(containsValueComparator.execute("contains", Any(), arrayOf("value 1")) as Boolean)
    }

    @Test
    fun testContainsWhenActualIsArray() {
        assertTrue(containsValueComparator.execute("contains", arrayOf("v1", "v2"), arrayOf("v1")) as Boolean)
    }

    @Test
    fun testContainsWhenActualIsArrayOfInteger() {
        assertTrue(containsValueComparator.execute("contains", arrayOf(1, 2, 3), arrayOf("2")) as Boolean)
    }

    @Test
    fun testContainsWhenActualIsList() {
        assertTrue(containsValueComparator.execute("contains", listOf(1, "v2"), arrayOf(1)) as Boolean)
        assertTrue(containsValueComparator.execute("contains", listOf(1, "v2"), arrayOf("v2")) as Boolean)
        assertFalse(containsValueComparator.execute("contains", listOf(10, 20, "30"), arrayOf(5)) as Boolean)
    }

    @Test
    fun testContainsWhenActualIsMap() {
        assertTrue(containsValueComparator.execute("contains", mapOf(Pair("k1", 1), Pair("k2", "v2")), arrayOf(1)) as Boolean)
        assertTrue(containsValueComparator.execute("contains", mapOf(Pair("k1", "1"), Pair("k2", "v2")), arrayOf("1")) as Boolean)
        assertTrue(containsValueComparator.execute("contains", mapOf(Pair("k1", "100"), Pair("k2", "v2")), arrayOf("v2")) as Boolean)
        assertFalse(containsValueComparator.execute("contains", mapOf(Pair("k2", "v2")), arrayOf(100)) as Boolean)
    }
}