package org.skellig.teststep.processing.validation.comparator

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.value.function.ContainsFunctionExecutor

class ContainsFunctionExecutorTest {

    private var containsFunctionExecutor = ContainsFunctionExecutor()

    @Test
    fun testContainsInString() {
        assertTrue(containsFunctionExecutor.execute("contains", "test value 1", arrayOf("value 1")) as Boolean)
    }

    @Test
    fun testNotContainsInString() {
        assertFalse(containsFunctionExecutor.execute("contains", "test value 1", arrayOf("boo")) as Boolean)
    }

    @Test
    fun testContainsWhenActualIsNull() {
        assertFalse(containsFunctionExecutor.execute("contains", null, arrayOf("value 1")) as Boolean)
    }

    @Test
    fun testContainsWhenActualIsObject() {
        assertFalse(containsFunctionExecutor.execute("contains", Any(), arrayOf("value 1")) as Boolean)
    }

    @Test
    fun testContainsWhenActualIsArray() {
        assertTrue(containsFunctionExecutor.execute("contains", arrayOf("v1", "v2"), arrayOf("v1")) as Boolean)
    }

    @Test
    fun testContainsWhenActualIsArrayOfInteger() {
        assertTrue(containsFunctionExecutor.execute("contains", arrayOf(1, 2, 3), arrayOf("2")) as Boolean)
    }

    @Test
    fun testContainsWhenActualIsList() {
        assertTrue(containsFunctionExecutor.execute("contains", listOf(1, "v2"), arrayOf(1)) as Boolean)
        assertTrue(containsFunctionExecutor.execute("contains", listOf(1, "v2"), arrayOf("v2")) as Boolean)
        assertFalse(containsFunctionExecutor.execute("contains", listOf(10, 20, "30"), arrayOf(5)) as Boolean)
    }

    @Test
    fun testContainsWhenActualIsMap() {
        assertTrue(containsFunctionExecutor.execute("contains", mapOf(Pair("k1", 1), Pair("k2", "v2")), arrayOf(1)) as Boolean)
        assertTrue(containsFunctionExecutor.execute("contains", mapOf(Pair("k1", "1"), Pair("k2", "v2")), arrayOf("1")) as Boolean)
        assertTrue(containsFunctionExecutor.execute("contains", mapOf(Pair("k1", "100"), Pair("k2", "v2")), arrayOf("v2")) as Boolean)
        assertFalse(containsFunctionExecutor.execute("contains", mapOf(Pair("k2", "v2")), arrayOf(100)) as Boolean)
    }
}