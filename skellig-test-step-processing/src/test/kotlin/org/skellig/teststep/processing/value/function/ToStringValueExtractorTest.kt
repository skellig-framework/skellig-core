package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.charset.Charset

class ToStringValueExtractorTest {

    private val extractor = ToStringValueExtractor()

    @Test
    fun testToStringByteArray() {
        val expectedValue = "hello"

        assertEquals(expectedValue, extractor.execute("toString", expectedValue.toByteArray(), emptyArray()))
    }

    @Test
    fun testToStringObject() {
        val expectedValue = Any()

        assertEquals(expectedValue.toString(), extractor.execute("toString", expectedValue, emptyArray()))
    }

    @Test
    fun testToStringWithCharset() {
        val charset = Charset.forName("utf16")
        val expectedValue = "hello".toByteArray(charset)

        assertEquals(expectedValue.toString(charset), extractor.execute("toString", expectedValue, arrayOf("utf16")))
    }
}