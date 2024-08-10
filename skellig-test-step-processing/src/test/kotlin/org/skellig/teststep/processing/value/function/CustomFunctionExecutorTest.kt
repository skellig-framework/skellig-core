package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import org.skellig.teststep.processing.value.exception.FunctionRegistryException

class CustomFunctionExecutorTest {

    @Test
    fun `register functions from class with default constructor`() {
        val converter = CustomFunctionExecutor(listOf("org.skellig.teststep.processing.value.function"))

        assertTrue(converter.execute("lessThan", null, arrayOf("1", "2")) as Boolean)
        assertFalse(converter.execute("isEmpty", null, arrayOf("gggg", "")) as Boolean)
        assertNull(converter.execute("runSomething", null, emptyArray())) // no return means null
        assertThrows(FunctionExecutionException::class.java) { converter.execute("runNotExisting", null, emptyArray()) }
    }

    @Test
    fun `register functions from class with no default constructor`() {
        val ex = assertThrows<FunctionRegistryException> {
            CustomFunctionExecutor(listOf("org.skellig.teststep.processing.invalid"))
        }
        assertEquals("Failed to instantiate class 'org.skellig.teststep.processing.invalid.InvalidCustomFunctions'", ex.message)
    }

    @Test
    fun `get function name as empty`() {
        assertEquals("", CustomFunctionExecutor(listOf("org.skellig.teststep.processing.value.function")).getFunctionName())
    }
}

class CustomFunctions {

    @Function
    fun lessThan(a: String, b: String): Boolean = a.toInt() < b.toInt()

    @Function
    fun isEmpty(a: String, empty: String): Boolean = a == empty

    @Function
    fun runSomething() {

    }
}