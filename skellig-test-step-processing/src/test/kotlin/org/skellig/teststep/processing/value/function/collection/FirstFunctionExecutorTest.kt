package org.skellig.teststep.processing.value.function.collection

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skellig.teststep.processing.value.exception.FunctionExecutionException

class FirstFunctionExecutorTest {

    private val functionExecutor = FirstFunctionExecutor()

    @Test
    fun `get first value from different collections`() {
        assertEquals(32, functionExecutor.execute("", listOf(32, 10, 90), emptyArray()))
        assertEquals(32, functionExecutor.execute("", arrayOf(32, 10, 90), emptyArray()))
        assertEquals(1, (functionExecutor.execute("", linkedMapOf(Pair("a", 1), Pair("b", 2)), emptyArray()) as Map.Entry<*, *>).value)
    }

    @Test
    fun `get first value from empty collections`() {
        assertThrows<NoSuchElementException> {
            functionExecutor.execute("", emptyList<String>(), emptyArray())
        }
    }

    @Test
    fun `get first value from any object`() {
        val ex = assertThrows<FunctionExecutionException> {
            functionExecutor.execute("", "any", emptyArray())
        }
        assertEquals("Function 'first' can only be called from array or collection of items", ex.message)
    }

    @Test
    fun `get first value from null`() {
        val ex = assertThrows<FunctionExecutionException> {
            functionExecutor.execute("", null, emptyArray())
        }
        assertEquals("Cannot get first item from null value when calling function 'first'", ex.message)
    }
}