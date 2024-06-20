package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skellig.teststep.processing.value.exception.FunctionExecutionException

class FromObjectFunctionExecutorTest {

    private var functionExecutor = FromObjectFunctionExecutor()

    @Test
    @DisplayName("Extract from null value")
    fun testExtractFromNullValue() {
        val ex = assertThrows<FunctionExecutionException> { functionExecutor.execute("f1", null, arrayOf()) }
        assertEquals("Cannot extract 'f1' from null value", ex.message)
    }

    @Test
    @DisplayName("Extract from Map with null value")
    fun testExtractFromMapNullValue() {
        assertNull(functionExecutor.execute("f1", mapOf(Pair("f1", null)), emptyArray()))
    }

    @Test
    @DisplayName("Extract from Map last String value")
    fun testExtractFromMapStringValue() {
        val f1 = functionExecutor.execute("f1", getTestMap(), emptyArray())
        val f2 = functionExecutor.execute("f2", f1, emptyArray())
        val f3 = functionExecutor.execute("f3", f2, emptyArray())

        assertEquals("v3", f3)
    }

    @Test
    @DisplayName("Extract from Map internal Map")
    fun testExtractFromMap() {
        val f1 = functionExecutor.execute("f1", getTestMap(), emptyArray())
        val f2 = functionExecutor.execute("f2", f1, emptyArray())

        assertTrue((f2 as Map<*, *>?)!!.containsKey("f3"))
    }

    @Test
    @DisplayName("Extract size of List")
    fun testExtractSizeOfList() {
        assertEquals(2, functionExecutor.execute("size", mutableListOf("v1", "v2"), emptyArray()))
    }

    @Test
    @DisplayName("Extract value of List by index")
    fun testExtractValueOfListByIndex() {
        assertEquals("v2", functionExecutor.execute("values[1]", mutableMapOf(Pair("values", mutableListOf("v1", "v2"))), emptyArray()))
    }

    @Test
    @DisplayName("Extract value of Array by index")
    fun testExtractValueOfArrayByIndex() {
        assertEquals("v2", functionExecutor.execute("values[1]", mutableMapOf(Pair("values", arrayOf("v1", "v2"))), emptyArray()))
    }

    @Test
    @DisplayName("Extract value by index from non-array")
    fun testExtractValueOfByIndexFromNonArray() {
        val ex = assertThrows<FunctionExecutionException> { functionExecutor.execute("values[1]", mutableMapOf(Pair("values", "random")), emptyArray()) }
        assertEquals("Cannot get value by index '1' from non array or list object: random", ex.message)
    }

    @Test
    @DisplayName("Extract size of Map")
    fun testExtractLengthOfArray() {
        assertEquals(1, functionExecutor.execute("size", functionExecutor.execute("f1", getTestMap(), emptyArray()), emptyArray()))
    }

    @Test
    @DisplayName("Extract from object of a class")
    fun testExtractFromCustomObject() {
        val testObject = TestObject("test")

        val name = functionExecutor.execute("name", testObject, emptyArray())
        assertEquals(testObject.name, name)
        assertEquals(testObject.name.length, functionExecutor.execute("length", name, emptyArray()))
    }

    @Test
    @DisplayName("Call interface method from object of a class")
    fun testCallInterfaceMethodOfClass() {
        val testObject = TestObject("test")

        functionExecutor.execute("callMethod", testObject, arrayOf(1))
        assertThrows<FunctionExecutionException> { functionExecutor.execute("callMethod", testObject, arrayOf(0)) }
    }

    @Test
    @DisplayName("Extract from private property of object of a class")
    fun testExtractFromCustomObjectWithPrivateProperty() {
        val testObject = TestObjectWithPrivateProperty("test")

        val ex = assertThrows<FunctionExecutionException> { functionExecutor.execute("name", testObject, emptyArray()) }
        assertEquals("No function or property `name` found in the result `TestObjectWithPrivateProperty(test)` with argument pairs ()", ex.message)
    }

    @Test
    @DisplayName("Call method of object which throws exception")
    fun testCallMethodWhichThrowsException() {
        val ex = assertThrows<FunctionExecutionException> { functionExecutor.execute("runWithException", this, emptyArray()) }
        assertEquals("Failed to call function `runWithException` of `FromObjectFunctionExecutorTest`", ex.message)
    }

    @Test
    @DisplayName("Extract from object where method not found")
    fun testExtractFromObjectWhenMethodNotFound() {
        assertThrows<FunctionExecutionException> { functionExecutor.execute("", Any(), arrayOf("param")) }
    }

    @Test
    @DisplayName("Extract by key with spaces and dots")
    fun testExtractByKeyWithSpacesAndDots() {
        val map = mapOf(
            Pair("a.b.c", 1),
            Pair("a b.c", 2),
            Pair("a.b ", mapOf(Pair("d", 3))),
        )

        assertAll(
            { assertEquals(1, functionExecutor.execute("a.b.c", map, arrayOf("a.b.c"))) },
            { assertEquals(2, functionExecutor.execute("a b.c", map, arrayOf("a b.c"))) },
            { assertEquals(3, functionExecutor.execute("d", functionExecutor.execute("a.b ", map, emptyArray()), emptyArray())) },
        )
    }

    @Test
    fun testGetFunctionName() {
       assertEquals("", functionExecutor.getFunctionName())
    }

    private fun getTestMap(): MutableMap<Any, Any> {
        return mutableMapOf(Pair("f1", mutableMapOf(Pair("f2", mutableMapOf(Pair("f3", "v3"))))))
    }

    fun runWithException() {
        throw RuntimeException("inner error")
    }

    override fun toString(): String = "FromObjectFunctionExecutorTest"

    private interface TestInterface {
        fun callMethod(times: Int)
    }

    private class TestObject(val name: String) : TestInterface {

        override fun callMethod(times: Int) {
            assertTrue(times > 0)
        }
    }

    private class TestObjectWithPrivateProperty(private val name: String) {
        override fun toString(): String = "TestObjectWithPrivateProperty($name)"
    }

}