package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.value.exception.FunctionExecutionException

class CustomFunctionExecutorTest {

    private val converter = CustomFunctionExecutor(listOf("org.skellig.teststep.processing.value.function"))

    @Test
    fun testConvertFromCustomFunction() {
        assertTrue(converter.execute("lessThan", null, arrayOf("1", "2")) as Boolean)
        assertFalse(converter.execute("isEmpty", null, arrayOf("gggg", "")) as Boolean)
        assertNull(converter.execute("runSomething", null, emptyArray())) // no return means null
        assertThrows(FunctionExecutionException::class.java) { converter.execute("runNotExisting", null, emptyArray()) }
    }
}

class CustomFunctions {

    @Function
    fun lessThan(a: String, b: String): Boolean = a.toInt() < b.toInt()

    @Function
    fun isEmpty(a: String, empty: String): Boolean = a == empty

    @Function
    fun runSomething() {

    }
}