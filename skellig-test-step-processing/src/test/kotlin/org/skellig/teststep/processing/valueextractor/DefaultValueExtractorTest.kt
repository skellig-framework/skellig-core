package org.skellig.teststep.processing.valueextractor

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.utils.UnitTestUtils

class DefaultValueExtractorTest {

    private var testStepValueExtractor: TestStepValueExtractor? = null

    @BeforeEach
    fun setUp() {
        testStepValueExtractor = DefaultValueExtractor.Builder().build()
    }

    @Test
    fun testExtractFromMap() {
        val extractionParameter = "k2"
        val value = UnitTestUtils.createMap("k1", "v1", extractionParameter, "v2")

        Assertions.assertEquals("v2", testStepValueExtractor!!.extract(value, extractionParameter))
    }

    @Test
    fun testExtractFromJson() {
        val value = "{ \"params\" : { \"f1\" : \"v1\" }}"

        Assertions.assertEquals("v1", testStepValueExtractor!!.extract(value, "json_path(params.f1)"))
    }

    @Test
    fun testExtractFromRegex() {
        val value = "{ params = { k1 = v1 }}"

        Assertions.assertEquals("v1", testStepValueExtractor!!.extract(value, "regex(k1 = (\\w+))"))
    }

    @Test
    fun testExtractWhenExtractorIsNull() {
        val value = "{ params = { k1 = v1 }}"

        Assertions.assertEquals(value, testStepValueExtractor!!.extract(value, null))
    }
}