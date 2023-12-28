package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.value.exception.FunctionExecutionException

class FromObjectFunctionExecutorTest {

    private var functionExecutor = FromObjectFunctionExecutor()

    @Test
    @DisplayName("Extract from Map with null value")
    fun testExtractFromMapNullValue() {
        Assertions.assertNull(functionExecutor.execute("f1", mapOf(Pair("f1", null)), emptyArray()))
    }

    @Test
    @DisplayName("Extract from Map last String value")
    fun testExtractFromMapStringValue() {
        val f1 = functionExecutor.execute("f1", getTestMap(), emptyArray())
        val f2 = functionExecutor.execute("f2", f1, emptyArray())
        val f3 = functionExecutor.execute("f3", f2, emptyArray())

        Assertions.assertEquals("v3", f3)
    }

    @Test
    @DisplayName("Extract from Map internal Map")
    fun testExtractFromMap() {
        val f1 = functionExecutor.execute("f1", getTestMap(), emptyArray())
        val f2 = functionExecutor.execute("f2", f1, emptyArray())

        Assertions.assertTrue((f2 as Map<*, *>?)!!.containsKey("f3"))
    }

    @Test
    @DisplayName("Extract size of List")
    fun testExtractSizeOfList() {
        Assertions.assertEquals(2, functionExecutor.execute("size", mutableListOf("v1", "v2"), emptyArray()))
    }

    @Test
    @DisplayName("Extract value of List by index")
    fun testExtractValueOfListByIndex() {
        Assertions.assertEquals("v2", functionExecutor.execute("values[1]", mutableMapOf(Pair("values", mutableListOf("v1", "v2"))), emptyArray()))
    }

    @Test
    @DisplayName("Extract size of Map")
    fun testExtractLengthOfArray() {
        Assertions.assertEquals(1, functionExecutor.execute("size", functionExecutor.execute("f1", getTestMap(), emptyArray()), emptyArray()))
    }

    @Test
    @DisplayName("Extract from object of a class")
    fun testExtractFromCustomObject() {
        val testObject = TestObject("test")

        val name = functionExecutor.execute("name", testObject, emptyArray())
        Assertions.assertEquals(testObject.name, name)
        Assertions.assertEquals(testObject.name.length, functionExecutor.execute("length", name, emptyArray()))
    }

    @Test
    @DisplayName("Extract from object where method not found")
    fun testExtractFromObjectWhenMethodNotFound() {
        Assertions.assertThrows(FunctionExecutionException::class.java) { functionExecutor.execute("", Any(), arrayOf("param")) }
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
            { Assertions.assertEquals(1, functionExecutor.execute("a.b.c", map, arrayOf("a.b.c"))) },
            { Assertions.assertEquals(2, functionExecutor.execute("a b.c", map, arrayOf("a b.c"))) },
            { Assertions.assertEquals(3, functionExecutor.execute("d", functionExecutor.execute("a.b ", map, emptyArray()), emptyArray())) },
        )
    }

    private fun getTestMap(): MutableMap<Any, Any> {
        return mutableMapOf(Pair("f1", mutableMapOf(Pair("f2", mutableMapOf(Pair("f3", "v3"))))))
    }

    private class TestObject(val name: String)

}