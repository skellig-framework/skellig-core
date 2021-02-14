package org.skellig.teststep.processing.valueextractor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.charset.Charset

class ToStringTestStepValueExtractorTest {

    val extractor = ToStringTestStepValueExtractor()

    @Test
    fun testToStringByteArray() {
        val expectedValue = "hello"

        assertEquals(expectedValue, extractor.extract(expectedValue.toByteArray(), ""))
    }

    @Test
    fun testToStringObject() {
        val expectedValue = Any()

        assertEquals(expectedValue.toString(), extractor.extract(expectedValue, ""))
    }

    @Test
    fun testToStringWithCharset() {
        val charset = Charset.forName("utf16")
        val expectedValue = "hello".toByteArray(charset)

        assertEquals(expectedValue.toString(charset), extractor.extract(expectedValue, "utf16"))
    }
}