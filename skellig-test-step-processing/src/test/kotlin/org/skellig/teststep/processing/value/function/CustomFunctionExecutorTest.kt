package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.exception.TestValueConversionException

class CustomFunctionExecutorTest {

    private val converter = CustomFunctionExecutor(
        listOf("org.skellig.teststep.processing.value.function"),
        CustomFunctionExecutorTest::class.java.classLoader
    )

    @Test
    fun testConvertFromCustomFunction() {
        assertTrue(converter.execute("lessThan", arrayOf("1", "2")) as Boolean)
        assertFalse(converter.execute("isEmpty", arrayOf("gggg", "")) as Boolean)
        assertNull(converter.execute("runSomething", emptyArray())) // no return means null
        assertThrows(TestValueConversionException::class.java) { converter.execute("runNotExisting", emptyArray()) }
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