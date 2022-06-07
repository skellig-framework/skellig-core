package org.skellig.teststep.processing.valueextractor

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.exception.ValueExtractionException
import java.util.*

class ObjectTestStepValueExtractorTest {

    private var valueExtractor: ObjectTestStepValueExtractor? = null

    @BeforeEach
    fun setUp() {
        valueExtractor = ObjectTestStepValueExtractor()
    }

    @Test
    @DisplayName("Extract from Map last String value")
    fun testExtractFromMapStringValue() {
        Assertions.assertEquals("v3", valueExtractor!!.extract(getTestMap(), "f1.f2.f3"))
    }

    @Test
    @DisplayName("Extract from Map internal Map")
    fun testExtractFromMap() {
        val response = valueExtractor!!.extract(getTestMap(), "f1.f2")

        Assertions.assertTrue((response as Map<*, *>?)!!.containsKey("f3"))
    }

    @Test
    @DisplayName("Extract size of List")
    fun testExtractSizeOfList() {
        val objects = ArrayList<Any>()
        objects.add("v1")
        objects.add("v2")

        Assertions.assertEquals(2, valueExtractor!!.extract(objects, "size"))
    }

    @Test
    @DisplayName("Extract size of Map")
    fun testExtractLengthOfArray() {
        Assertions.assertEquals(1, valueExtractor!!.extract(getTestMap(), "f1.size"))
    }

    @Test
    @DisplayName("Extract from object of a class")
    fun testExtractFromCustomObject() {
        val testObject = TestObject("test")

        Assertions.assertEquals(testObject.name, valueExtractor!!.extract(testObject, "name"))
        Assertions.assertEquals(testObject.name!!.length, valueExtractor!!.extract(testObject, "name.length"))
    }

    @Test
    @DisplayName("Extract from object of a class When property is null")
    fun testExtractFromCustomObjectWhenPropertyIsNull() {
        val testObject = TestObject(null)

        Assertions.assertNull(valueExtractor!!.extract(testObject, "name"))
    }

    @Test
    @DisplayName("Extract from object where method not found")
    fun testExtractFromObjectWhenMethodNotFound() {
        Assertions.assertThrows(ValueExtractionException::class.java) { valueExtractor!!.extract(Any(), "param") }
    }

    @Test
    @DisplayName("Extract by key with spaces and dots")
    fun testExtractByKeyWithSpacesAndDots() {
        val map = mapOf(
                Pair("a.b.c", 1),
                Pair("a b.c", 2),
                Pair("a.b ", mapOf(Pair("d", 3))),
        )

        Assertions.assertAll(
                { Assertions.assertEquals(1, valueExtractor!!.extract(map, "\"a.b.c\"")) },
                { Assertions.assertEquals(1, valueExtractor!!.extract(map, "'a.b.c'")) },
                { Assertions.assertEquals(2, valueExtractor!!.extract(map, "'a b.c'")) },
                { Assertions.assertEquals(3, valueExtractor!!.extract(map, "'a.b '.d")) },
        )

    }

    private fun getTestMap(): MutableMap<Any, Any> {
        return mutableMapOf(Pair("f1", mutableMapOf(Pair("f2", mutableMapOf(Pair("f3", "v3"))))))
    }

    private class TestObject(val name: String?)

}