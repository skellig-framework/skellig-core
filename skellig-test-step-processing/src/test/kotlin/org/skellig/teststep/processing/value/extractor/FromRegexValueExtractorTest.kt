package org.skellig.teststep.processing.value.extractor

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.value.extractor.exception.ValueExtractionException
import org.skellig.teststep.processing.value.function.exception.FunctionValueExecutionException

class FromRegexValueExtractorTest {

    private var regexValueExtractor = FromRegexValueExtractor()

    @Test
    fun testExtractByRegexFromNull() {
        Assertions.assertThrows(ValueExtractionException::class.java) {
            regexValueExtractor.extractFrom("fromRegex", null, arrayOf("(\\w+)"))
        }
    }

    @Test
    fun testExtractByRegex() {
        val regexFilter = ".*id\\s*=\\s*([A-Z]{2}\\d{4}).*"

        Assertions.assertEquals(
            "NM1100",
            regexValueExtractor.extractFrom("fromRegex", "log data: id = NM1100, name = event", arrayOf(regexFilter))
        )
    }

    @Test
    fun testExtractByRegexWithoutGroups() {
        val regexFilter = "f1=\\w+"

        Assertions.assertEquals(
            "f1=v1",
            regexValueExtractor.extractFrom("fromRegex", "some data f1=v1 some data", arrayOf(regexFilter))
        )
    }

    @Test
    fun testExtractByRegexManyGroups() {
        val regexFilter = "f1=(\\w+),.*f2=(\\w+)"

        Assertions.assertEquals(
            listOf("v1", "v2"),
            regexValueExtractor.extractFrom("fromRegex", "some data f1=v1, some data f2=v2", arrayOf(regexFilter))
        )
    }

    @Test
    fun testExtractByRegexWhenNoMatch() {
        val regexFilter = "data: ([a-z]+)"
        val value = "data: 1000"

        Assertions.assertEquals(value, regexValueExtractor.extractFrom("fromRegex", value, arrayOf(regexFilter)))
    }

    @Test
    fun testExtractByRegexWithInvalidNumberOfArguments() {
        val ex = Assertions.assertThrows(FunctionValueExecutionException::class.java) { regexValueExtractor.extractFrom("fromRegex", "regex", arrayOf("v1", "v2")) }

        Assertions.assertEquals("Function `regex` can only accept 1 argument. Found 2", ex.message)
    }
}