package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.value.function.exception.FunctionValueExecutionException
import java.math.BigDecimal

class CalcFunctionExecutorTest {

    private val functionExecutor = CalcFunctionExecutor()

    @Test
    fun testVariousExpressions() {
        assertAll(
            { assertEquals(BigDecimal(7), functionExecutor.execute("calc", arrayOf("7"))) },
            { assertEquals(BigDecimal(4), functionExecutor.execute("calc", arrayOf("-3+7"))) },
            { assertEquals(BigDecimal("-2"), functionExecutor.execute("calc", arrayOf("2*(-1)"))) },
            { assertEquals(BigDecimal(3), functionExecutor.execute("calc", arrayOf("1+2"))) },
            { assertEquals(BigDecimal("31.2"), functionExecutor.execute("calc", arrayOf("1.2+30"))) },
            { assertEquals(BigDecimal(50), functionExecutor.execute("calc", arrayOf("(100+900)/20"))) },
            { assertEquals(BigDecimal(13), functionExecutor.execute("calc", arrayOf("((1+2)*3)+4"))) },
            { assertEquals(BigDecimal(21), functionExecutor.execute("calc", arrayOf("(1+2)*(3+4)"))) },
        )
    }

    @Test
    fun testWithInvalidExpression() {
        assertThrows(FunctionValueExecutionException::class.java) {functionExecutor.execute("calc", arrayOf("random")) }
        assertThrows(FunctionValueExecutionException::class.java) {functionExecutor.execute("calc", arrayOf("1+d")) }
        assertThrows(FunctionValueExecutionException::class.java) {functionExecutor.execute("calc", arrayOf("()")) }
        assertThrows(FunctionValueExecutionException::class.java) {functionExecutor.execute("calc", arrayOf("-5-+-1")) }
    }
}