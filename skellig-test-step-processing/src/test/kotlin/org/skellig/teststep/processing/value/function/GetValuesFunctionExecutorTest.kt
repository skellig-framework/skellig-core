package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skellig.teststep.processing.value.exception.FunctionExecutionException

class GetValuesFunctionExecutorTest {

    private lateinit var getValuesFunctionExecutor: GetValuesFunctionExecutor

    @BeforeEach
    fun setUp() {
        getValuesFunctionExecutor = GetValuesFunctionExecutor()
    }

    @Test
    fun `get value from collection`() {
        val collection = listOf("item1", "item2")
        val result = getValuesFunctionExecutor.execute("getValues", collection, emptyArray())
        assertEquals(collection, result)
    }

    @Test
    fun `get value from array`() {
        val array = arrayOf("element1", "element2")
        val result = getValuesFunctionExecutor.execute("getValues", array, emptyArray())
        assertTrue(result is Array<*>)
        assertArrayEquals(array, result as Array<*>)
    }

    @Test
    fun `get value from map`() {
        val map = mapOf("one" to 1, "two" to 2)
        val result = getValuesFunctionExecutor.execute("getValues", map, emptyArray())
        assertEquals(map.values, result)
    }

    @Test
    fun `get value from null`() {
        val exception = assertThrows<FunctionExecutionException> {
            getValuesFunctionExecutor.execute("getValues", null, emptyArray())
        }
        assertEquals("Cannot get values from null value", exception.message)
    }

    @Test
    fun `get value from string`() {
        val value = "I'm just a string"
        val result = getValuesFunctionExecutor.execute("getValues", value, emptyArray())
        assertEquals(value, result)
    }

    @Test
    fun `get function name`() {
        assertEquals("getValues", getValuesFunctionExecutor.getFunctionName())
    }
}