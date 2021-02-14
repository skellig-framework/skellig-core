package org.skellig.teststep.processing.valueextractor

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RegexTestStepValueExtractorTest {

    private var regexValueExtractor: RegexTestStepValueExtractor? = null

    @BeforeEach
    fun setUp() {
        regexValueExtractor = RegexTestStepValueExtractor()
    }

    @Test
    fun testExtractByRegex() {
        val regexFilter = ".*id\\s*=\\s*([A-Z]{2}\\d{4}).*"

        Assertions.assertEquals("NM1100",
                regexValueExtractor!!.extract("log data: id = NM1100, name = event", regexFilter))
    }

    @Test
    fun testExtractByRegexWithoutGroups() {
        val regexFilter = "f1=\\w+"

        Assertions.assertEquals("f1=v1",
                regexValueExtractor!!.extract("some data f1=v1 some data", regexFilter))
    }

    @Test
    fun testExtractByRegexManyGroups() {
        val regexFilter = "f1=(\\w+),.*f2=(\\w+)"

        Assertions.assertEquals(listOf("v1", "v2"),
                regexValueExtractor!!.extract("some data f1=v1, some data f2=v2", regexFilter))
    }

    @Test
    fun testExtractByRegexWhenNoMatch() {
        val regexFilter = "data: ([a-z]+)"
        val value = "data: 1000"

        Assertions.assertEquals(value, regexValueExtractor!!.extract(value, regexFilter))
    }
}