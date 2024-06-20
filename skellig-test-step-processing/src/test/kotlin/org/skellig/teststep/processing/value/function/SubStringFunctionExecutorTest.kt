package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skellig.teststep.processing.value.exception.FunctionExecutionException

internal class SubStringFunctionExecutorTest {

    private val functionExecutor = SubStringFunctionExecutor()

    @Test
    fun testSubstringWithNullParams() {
        assertEquals("a/b/c", functionExecutor.execute("subString", "a/b/c", arrayOf(null)))
    }

    @Test
    fun testSubstringWithoutParams() {
        assertEquals("a/b/c", functionExecutor.execute("subString", "a/b/c", arrayOf("")))
    }

    @Test
    fun testSubstringWithNonExistentDelimiter() {
        assertEquals("a/b/c", functionExecutor.execute("subString", "a/b/c", arrayOf(",")))
    }

    @Test
    fun testSubstringFirst() {
        assertEquals("b/c", functionExecutor.execute("subString", "a/b/c", arrayOf("/")))
    }

    @Test
    fun testSubstringOfNull() {
        val ex = assertThrows<FunctionExecutionException> { functionExecutor.execute("subString", null, arrayOf("/")) }
        assertEquals("Cannot extract sub string '/' from null value", ex.message)
    }

    @Test
    fun testSubstringWithEmptyArgs() {
        val ex = assertThrows<FunctionExecutionException> { functionExecutor.execute("subString", "value", emptyArray()) }
        assertEquals("Function `subString` can only accept 1 String argument. Found 0", ex.message)
    }
}