package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.charset.Charset

class ToStringFunctionExecutorTest {

    private val functionExecutor = ToStringFunctionExecutor()

    @Test
    fun testToStringByteArray() {
        val expectedValue = "hello"

        assertEquals(expectedValue, functionExecutor.execute("toString", expectedValue.toByteArray(), emptyArray()))
    }

    @Test
    fun testToStringObject() {
        val expectedValue = Any()

        assertEquals(expectedValue.toString(), functionExecutor.execute("toString", expectedValue, emptyArray()))
    }

    @Test
    fun testToStringWithCharset() {
        val charset = Charset.forName("utf16")
        val expectedValue = "hello".toByteArray(charset)

        assertEquals(expectedValue.toString(charset), functionExecutor.execute("toString", expectedValue, arrayOf("utf16")))
    }

    @Test
    fun testToStringNullValue() {
        assertEquals("null", functionExecutor.execute("toString", null, emptyArray()))
    }
}