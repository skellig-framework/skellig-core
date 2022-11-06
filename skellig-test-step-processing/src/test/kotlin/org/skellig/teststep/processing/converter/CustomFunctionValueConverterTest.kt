package org.skellig.teststep.processing.converter

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CustomFunctionValueConverterTest {

    private val converter = CustomFunctionValueConverter(listOf("org.skellig.teststep.processing.converter"),
        CustomFunctionValueConverterTest::class.java.classLoader)

    @Test
    fun testConvertFromCustomFunction() {
        assertTrue(converter.convert("lessThan(1, 2)") as Boolean)
        assertFalse(converter.convert("lessThan( 2, 1 )") as Boolean)
        assertFalse(converter.convert("isEmpty(gggg, )") as Boolean)
        assertNull(converter.convert("runSomething()")) // no return means null
        assertEquals("runNotExisting()", converter.convert("runNotExisting()"))
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