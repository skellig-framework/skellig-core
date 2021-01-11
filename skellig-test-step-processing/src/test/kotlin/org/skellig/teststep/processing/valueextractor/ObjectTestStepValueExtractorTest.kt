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
    @DisplayName("Extract from List of Map last String value")
    fun testExtractFromMapAndList() {
        val testMap = getTestMap()
        testMap["f4"] = listOf(Collections.singletonMap("f5", "v5"))

        Assertions.assertEquals("v5", valueExtractor!!.extract(listOf<Map<Any, Any>>(testMap), "[0].f4.[0].f5"))
    }

    @Test
    @DisplayName("Extract from List last String value")
    fun testExtractFromList() {
        Assertions.assertEquals("v1", valueExtractor!!.extract(Arrays.asList("v1", "v2"), "[0]"))
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
    @DisplayName("Extract from Array last String value")
    fun testExtractFromArray() {
        Assertions.assertEquals("v1", valueExtractor!!.extract(arrayOf("v1", "v2"), "[0]"))
    }

    @Test
    @DisplayName("Extract from List of List last String value")
    fun testExtractFromListOfList() {
        Assertions.assertEquals("v2", valueExtractor!!.extract(listOf(Arrays.asList("v1", "v2")), "[0].[1]"))
    }

    @Test
    @DisplayName("Extract from object of a class")
    fun testExtractFromCustomObject() {
        val testObject = TestObject("test")

        Assertions.assertEquals(testObject.name, valueExtractor!!.extract(testObject, "name"))
        Assertions.assertEquals(testObject.name.length, valueExtractor!!.extract(testObject, "name.length"))
    }

    @Test
    @DisplayName("Extract from object of a class with List and Map inside")
    fun testExtractFromCustomObjectWithListAndMap() {
        val `object` = listOf(Collections.singletonMap<String, Any>("f1",
                ComplexTestObject(Collections.singletonMap<String, Any>("f2", TestObject("test")))))

        Assertions.assertEquals("test", valueExtractor!!.extract(`object`, "[0].f1.params.f2.name"))
    }

    @Test
    @DisplayName("Extract from object where method not found")
    fun testExtractFromObjectWhenMethodNotFound() {
        Assertions.assertThrows(ValueExtractionException::class.java) { valueExtractor!!.extract(Any(), "param") }
    }

    private fun getTestMap(): MutableMap<Any, Any> {
        return mutableMapOf(Pair("f1", mutableMapOf(Pair("f2", mutableMapOf(Pair("f3", "v3"))))))
    }

    private class TestObject(val name: String)

    private class ComplexTestObject(val params: Map<String, Any>)
}