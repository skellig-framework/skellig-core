package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skellig.teststep.processing.value.exception.FunctionExecutionException

internal class JsonPathFunctionExecutorTest {

    private val extractor = JsonPathFunctionExecutor()

    @Test
    fun testExtractFromJson() {
        assertEquals("1", extractor.execute("jsonPath", createJson(), arrayOf("a")))
    }

    @Test
    fun testExtractFromJsonValueWhichDoesNotExist() {
        assertNull(extractor.execute("jsonPath", createJson(), arrayOf("ggg")))
    }

    @Test
    fun testExtractComplexValueFromJson() {
        assertEquals("[d:v2, g:[1, 2, 3]]", extractor.execute("jsonPath", createJson(), arrayOf("c")))
    }

    @Test
    fun testExtractFromInvalidJson() {
        val ex = assertThrows(FunctionExecutionException::class.java) {
            extractor.execute("jsonPath", "invalid", arrayOf("b"))
        }

        assertEquals("Failed to extract jsonPath 'b' from value 'invalid'. Reason Failed to parse the JSON document", ex.message)
    }

    @Test
    fun testExtractFromInvalidJsonWithSkipFailure() {
        assertNull(extractor.execute("jsonPath", "invalid json", arrayOf("c", "true")))
    }

    @Test
    fun testExtractFromNullJson() {
        assertNull(extractor.execute("jsonPath", null, arrayOf("c")))
    }

    @Test
    fun testExtractByEmptyJsonPath() {
        assertEquals("[a:1, b:v1, c:[d:v2, g:[1, 2, 3]]]", extractor.execute("jsonPath", createJson(), arrayOf("")))
    }

    @Test
    fun testExtractFromJsonWithEmptyArgs() {
        val ex = assertThrows<FunctionExecutionException> {
            extractor.execute("jsonPath", "invalid", emptyArray())
        }
        assertEquals("Function `jsonPath` can only accept 1 or 2 arguments. Found 0", ex.message)
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
