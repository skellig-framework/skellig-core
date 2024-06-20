package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import java.math.BigDecimal

class ToNumberFunctionExecutorTest {

    @Test
    fun testToNumber() {
        assertEquals(BigDecimal("100"), ToBigDecimalFunctionExecutor().execute("", "100", emptyArray()))
    }

    @Test
    fun testToDoubleFromInteger() {
        assertEquals(100.0, ToDoubleFunctionExecutor().execute("", 100, emptyArray()))
    }

    @Test
    fun `convert to byte`() {
        val functionExecutor = ToByteFunctionExecutor()
        assertEquals(100.toByte(), functionExecutor.execute("", 100, emptyArray()))
        assertEquals(100.toByte(), functionExecutor.execute("", "100", emptyArray()))
    }

    @Test
    fun `convert to short`() {
        val functionExecutor = ToShortFunctionExecutor()
        assertEquals(100.toShort(), functionExecutor.execute("", 100, emptyArray()))
        assertEquals(100.toShort(), functionExecutor.execute("", "100", emptyArray()))
    }

    @Test
    fun `convert to long`() {
        val functionExecutor = ToLongFunctionExecutor()
        assertEquals(100000000.toLong(), functionExecutor.execute("", 100000000, emptyArray()))
        assertEquals(100000000.toLong(), functionExecutor.execute("", "100000000", emptyArray()))
    }

    @Test
    fun `convert to float`() {
        val functionExecutor = ToFloatFunctionExecutor()
        assertEquals(100.43f, functionExecutor.execute("", 100.43, emptyArray()))
        assertEquals(100.43f, functionExecutor.execute("", "100.43", emptyArray()))
    }

    @Test
    fun `convert to boolean`() {
        val functionExecutor = ToBooleanFunctionExecutor()
        assertEquals(true, functionExecutor.execute("", true, emptyArray()))
        assertEquals(false, functionExecutor.execute("", "false", emptyArray()))
        assertEquals(true, functionExecutor.execute("", "true", emptyArray()))
    }

    @Test
    fun testToNumberWhenNotString() {
        assertThrows<FunctionExecutionException>("Failed to extract numeric value from type class kotlin.collections.EmptyList")
        { ToBigDecimalFunctionExecutor().execute("", listOf<String>(), emptyArray()) }
    }

    @Test
    fun testToNumberWhenNull() {
        assertThrows<FunctionExecutionException>("Failed to extract numeric value from type null")
        { ToBigDecimalFunctionExecutor().execute("", null, emptyArray()) }
    }
}