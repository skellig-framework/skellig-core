package org.skellig.teststep.processing.valueextractor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.skellig.teststep.processing.utils.UnitTestUtils
import java.util.*

class DefaultValueExtractorTest {

    private var testStepValueExtractor = DefaultValueExtractor.Builder().build()

    @Nested
    inner class SimpleExtractionsTest {

        @Test
        fun testExtractFromMap() {
            val extractionParameter = "k2"
            val value = UnitTestUtils.createMap("k1", "v1", extractionParameter, "v2")

            assertEquals("v2", testStepValueExtractor.extract(value, extractionParameter))
        }

        @Test
        fun testExtractFromJson() {
            val value = "{ \"params\" : { \"f1\" : \"v1\" }}"

            assertEquals("v1", testStepValueExtractor.extract(value, "jsonPath(params.f1)"))
        }

        @Test
        fun testExtractFromRegex() {
            val value = "{ params = { k1 = v1 }}"

            assertEquals("v1", testStepValueExtractor.extract(value, "regex(k1 = (\\w+))"))
        }

        @Test
        fun testExtractWhenExtractorIsNull() {
            val value = "{ params = { k1 = v1 }}"

            assertEquals(value, testStepValueExtractor.extract(value, null))
        }

        @Test
        fun testExtractFromMapAndJson() {
            val value = UnitTestUtils.createMap("body", "{ \"params\" : { \"f1\" : \"v1\" }}")

            assertEquals("v1", testStepValueExtractor.extract(value, "body.jsonPath(params.f1)"))
        }
    }

    @Nested
    inner class CollectionsAndMapExtractionsTest {

        @Test
        fun testExtractFromList2() {
            val value = UnitTestUtils.createMap("a", listOf("v1", "v2"))

            assertAll(
                    { assertEquals("v1", testStepValueExtractor.extract(value, "a.fromIndex(0)")) },
                    { assertEquals("v2", testStepValueExtractor.extract(value, "a.fromIndex(1)")) }
            )
        }

        @Test
        @DisplayName("Extract from List of Map last String value")
        fun testExtractFromMapAndList() {
            val testMap = mapOf(Pair("f1", mapOf(Pair("f2", mapOf(Pair("f3", "v3"))))), Pair("f4", listOf(mapOf(Pair("f5", "v5")))))

            assertEquals("v5", testStepValueExtractor.extract(listOf(testMap), "fromIndex(0).f4.fromIndex(0).f5"))
        }

        @Test
        @DisplayName("Extract from List last String value")
        fun testExtractFromList() {
            assertEquals("v1", testStepValueExtractor.extract(listOf("v1", "v2"), "fromIndex(0)"))
        }

        @Test
        @DisplayName("Extract from Array last String value")
        fun testExtractFromArray() {
            assertEquals("v1", testStepValueExtractor.extract(arrayOf("v1", "v2"), "fromIndex(0)"))
        }

        @Test
        @DisplayName("Extract from List of List last String value")
        fun testExtractFromListOfList() {
            assertEquals("v2", testStepValueExtractor.extract(listOf(listOf("v1", "v2")), "fromIndex(0).fromIndex(1)"))
        }

        @Test
        @DisplayName("Extract from object of a class with List and Map inside")
        fun testExtractFromCustomObjectWithListAndMap() {
            val o = listOf(Collections.singletonMap<String, Any>("f1",
                    SimpleObject(Collections.singletonMap<String, Any>("f2", TestObject("test")))))

            assertEquals("test", testStepValueExtractor.extract(o, "fromIndex(0).f1.params.f2.name"))
        }
    }

    @Nested
    inner class ComplexExtractionsTest {

        @Test
        fun testExtractFromManyObjects() {
            val value = SimpleObject(UnitTestUtils.createMap("json.body", "{ \"array\" : [ \"1\", \"a=b=ccc\" ]}"))

            assertEquals(3, testStepValueExtractor.extract(value,
                    "params.'json.body'.jsonPath(array[1]).subStringLast(=).length"))
        }

        @Test
        fun testExtractFromManyObjectsAsList() {
            val value = SimpleObject(mapOf(Pair("data", "f0=0, f1=(v1),f2=v2,f3 = 'v3'")))

            assertEquals(listOf("(v1)", "'v3'"), testStepValueExtractor.extract(value,
                    "params.data.regex('.*f1=(\\(\\w+\\)).*f3 = (\\'\\w+\\')')"))
        }

        @Test
        fun testExtractFromManyObjectsWithConcatenation() {
            val value = SimpleObject(mapOf(Pair("data", """{ "a": 1 }""".toByteArray())))

            assertEquals("1", testStepValueExtractor.extract(value,
                    "params.data.toString().jsonPath(a)"))
            assertEquals("1_._", testStepValueExtractor.extract(value,
                    "params.data.toString(utf8).jsonPath(a).concat(_._)"))
        }

    }

    inner class SimpleObject(val params: Map<String, Any?>)

    inner class TestObject(val name: String)
}