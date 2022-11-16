package org.skellig.teststep.processing.converter

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CustomFunctionValueConverterTest {

    private val converter = CustomFunctionValueConverter(listOf("org.skellig.teststep.processing.converter"),
        CustomFunctionValueConverterTest::class.java.classLoader)

    @Test
    fun testConvertFromCustomFunction() {
        assertTrue(converter.execute("lessThan", arrayOf("1", "2")) as Boolean)
        assertFalse(converter.execute("isEmpty", arrayOf("gggg", "")) as Boolean)
        assertNull(converter.execute("runSomething", emptyArray())) // no return means null
        assertNull(converter.execute("runNotExisting", emptyArray()))
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