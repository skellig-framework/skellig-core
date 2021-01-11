package org.skellig.teststep.processing.valueextractor

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.utils.UnitTestUtils

class DefaultValueExtractorTest {

    private var testStepValueExtractor = DefaultValueExtractor.Builder().build()

    @Test
    fun testExtractFromMap() {
        val extractionParameter = "k2"
        val value = UnitTestUtils.createMap("k1", "v1", extractionParameter, "v2")

        Assertions.assertEquals("v2", testStepValueExtractor.extract(value, extractionParameter))
    }

    @Test
    fun testExtractFromJson() {
        val value = "{ \"params\" : { \"f1\" : \"v1\" }}"

        Assertions.assertEquals("v1", testStepValueExtractor.extract(value, "jsonPath(params.f1)"))
    }

    @Test
    fun testExtractFromRegex() {
        val value = "{ params = { k1 = v1 }}"

        Assertions.assertEquals("v1", testStepValueExtractor.extract(value, "regex(k1 = (\\w+))"))
    }

    @Test
    fun testExtractWhenExtractorIsNull() {
        val value = "{ params = { k1 = v1 }}"

        Assertions.assertEquals(value, testStepValueExtractor.extract(value, null))
    }

    @Test
    fun testExtractFromMapAndJson() {
        val value = UnitTestUtils.createMap("body", "{ \"params\" : { \"f1\" : \"v1\" }}")

        Assertions.assertEquals("v1", testStepValueExtractor.extract(value, "body.jsonPath(params.f1)"))
    }

    @Test
    fun testExtractFromManyObjects() {
        val value = SimpleObject(UnitTestUtils.createMap("json.body", "{ \"array\" : [ \"1\", \"a=b=ccc\" ]}"))

        Assertions.assertEquals(3, testStepValueExtractor.extract(value,
                "params.'json.body'.jsonPath(array[1]).subStringLast(=).length"))
    }

    inner class SimpleObject(val params: Map<String, Any?>)
}