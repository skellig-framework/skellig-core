package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skellig.teststep.processing.value.exception.FunctionExecutionException

class SizeFunctionExecutorTest {
    private val functionExecutor = SizeFunctionExecutor()

    @Test
    fun `get size of list`() {
        val dummyList = listOf(1,2,3)
        val result = functionExecutor.execute("size", dummyList, arrayOf())
        assertEquals(3, result)
    }

    @Test
    fun `get size of array`() {
        val dummyArray = arrayOf(1,2,3)
        val result = functionExecutor.execute("size", dummyArray, arrayOf())
        assertEquals(3, result)
    }

    @Test
    fun `get size of map`() {
        val dummyMap = mapOf("One" to 1,"Two" to 2)
        val result = functionExecutor.execute("size", dummyMap, arrayOf())
        assertEquals(2, result)
    }

    @Test
    fun `get size of string`() {
        val dummyString = "Hello"
        val result = functionExecutor.execute("size", dummyString, arrayOf())
        assertEquals(5, result)
    }

    @Test
    fun `get size of invalid type`() {
        val dummyValue = 123
        val exception = assertThrows<FunctionExecutionException> {
            functionExecutor.execute("size", dummyValue, arrayOf())
        }
        assertEquals("Value is invalid type for function 'size'", exception.message)
    }

    @Test
    fun `get size of null`() {
        val exception = assertThrows<FunctionExecutionException> {
            functionExecutor.execute("size", null, arrayOf())
        }
        assertEquals("Function 'size' cannot be called from null value", exception.message)
    }
}