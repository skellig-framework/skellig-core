package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.value.exception.FunctionExecutionException

class ObjectValueExtractorTest {

    private var valueExtractor = ObjectValueExtractor()

    @Test
    @DisplayName("Extract from Map with null value")
    fun testExtractFromMapNullValue() {
        Assertions.assertNull(valueExtractor.execute("", mapOf(Pair("f1", null)), arrayOf("f1")))
    }

    @Test
    @DisplayName("Extract from Map last String value")
    fun testExtractFromMapStringValue() {
        Assertions.assertEquals("v3", valueExtractor.execute("", getTestMap(), arrayOf("f1", "f2", "f3")))
    }

    @Test
    @DisplayName("Extract from Map internal Map")
    fun testExtractFromMap() {
        val response = valueExtractor.execute("", getTestMap(), arrayOf("f1", "f2"))

        Assertions.assertTrue((response as Map<*, *>?)!!.containsKey("f3"))
    }

    @Test
    @DisplayName("Extract size of List")
    fun testExtractSizeOfList() {
        Assertions.assertEquals(2, valueExtractor.execute("size", mutableListOf("v1", "v2"), emptyArray()))
    }

    @Test
    @DisplayName("Extract value of List by index")
    fun testExtractValueOfListByIndex() {
        Assertions.assertEquals("v2", valueExtractor.execute("", mutableMapOf(Pair("values", mutableListOf("v1", "v2"))), arrayOf("values[1]")))
    }

    @Test
    @DisplayName("Extract size of Map")
    fun testExtractLengthOfArray() {
        Assertions.assertEquals(1, valueExtractor.execute("size", valueExtractor.execute("", getTestMap(), arrayOf("f1")), emptyArray()))
    }

    @Test
    @DisplayName("Extract from object of a class")
    fun testExtractFromCustomObject() {
        val testObject = TestObject("test")

        val name = valueExtractor.execute("", testObject, arrayOf("name"))
        Assertions.assertEquals(testObject.name, name)
        Assertions.assertEquals(testObject.name.length, valueExtractor.execute("length", name, emptyArray()))
    }

    @Test
    @DisplayName("Extract from object where method not found")
    fun testExtractFromObjectWhenMethodNotFound() {
        Assertions.assertThrows(FunctionExecutionException::class.java) { valueExtractor.execute("", Any(), arrayOf("param")) }
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
            { Assertions.assertEquals(1, valueExtractor.execute("", map, arrayOf("a.b.c"))) },
            { Assertions.assertEquals(1, valueExtractor.execute("", map, arrayOf("a.b.c"))) },
            { Assertions.assertEquals(2, valueExtractor.execute("", map, arrayOf("a b.c"))) },
            { Assertions.assertEquals(3, valueExtractor.execute("", map, arrayOf("a.b ", "d"))) },
        )
    }

    private fun getTestMap(): MutableMap<Any, Any> {
        return mutableMapOf(Pair("f1", mutableMapOf(Pair("f2", mutableMapOf(Pair("f3", "v3"))))))
    }

    private class TestObject(val name: String)

}