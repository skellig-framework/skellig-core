package org.skellig.teststep.processing.valueextractor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.exception.ValueExtractionException

internal class JsonPathTestStepValueExtractorTest {

    private val extractor = JsonPathTestStepValueExtractor()

    @Test
    fun testExtractFromJson() {
        assertEquals("1", extractor.extract(createJson(), "a"))
    }

    @Test
    fun testExtractFromJsonValueWhichDoesNotExist() {
        assertNull(extractor.extract(createJson(), "ggg"))
    }

    @Test
    fun testExtractComplexValueFromJson() {
        assertEquals("[d:v2, g:[1, 2, 3]]", extractor.extract(createJson(), "c"))
    }

    @Test
    fun testExtractFromInvalidJson() {
        val ex = assertThrows(ValueExtractionException::class.java) {
            extractor.extract("invalid", "b")
        }

        assertEquals("Failed to extract jsonPath 'b' from value 'invalid'. Reason Failed to parse the JSON document", ex.message)
    }

    @Test
    fun testExtractFromInvalidJsonWithSkipFailure() {
        assertNull(extractor.extract("invalid json", "c, true"))
    }

    @Test
    fun testExtractFromNullJson() {
        assertNull(extractor.extract(null, "c"))
    }

    @Test
    fun testExtractByEmptyJsonPath() {
        assertEquals("[a:1, b:v1, c:[d:v2, g:[1, 2, 3]]]", extractor.extract(createJson(), ""))
    }

    private fun createJson() = """
                {
                   "a": 1,
                   "b": "v1",
                   "c": {
                      "d": "v2",
                      "g": [1,2,3]
                   }
                }
            """.trimIndent()
}
