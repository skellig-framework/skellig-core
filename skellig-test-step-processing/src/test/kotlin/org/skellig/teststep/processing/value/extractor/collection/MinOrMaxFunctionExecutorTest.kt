package org.skellig.teststep.processing.value.extractor.collection

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class MinOrMaxFunctionExecutorTest {

    private val minFunctionExecutor = MinOfFunctionExecutor()
    private val maxOfFunctionExecutor = MaxOfFunctionExecutor()
    private val intValues = listOf(mapOf(Pair("price", 1)), mapOf(Pair("price", 3)))
    private val doubleValues = listOf(mapOf(Pair("price", 3.09)), mapOf(Pair("price", 1.1)), mapOf(Pair("price", 1.005)))
    private val stringValues = listOf(mapOf(Pair("price", "15.1")), mapOf(Pair("price", "12.015")))
    private val bigDecimalValues = listOf(BigDecimal(100), BigDecimal(15), BigDecimal(40))
    private val defaultPredicate = { v: Any? -> (v as Map<*, *>)["price"] }

    @Test
    fun testMin() {
        assertEquals(BigDecimal(1), minFunctionExecutor.extractFrom("minOf", intValues, arrayOf(defaultPredicate)))
        assertEquals(BigDecimal("1.005"), minFunctionExecutor.extractFrom("minOf", doubleValues, arrayOf(defaultPredicate)))
        assertEquals(BigDecimal("12.015"), minFunctionExecutor.extractFrom("minOf", stringValues, arrayOf(defaultPredicate)))
        assertEquals(
            BigDecimal(15), minFunctionExecutor.extractFrom("minOf", bigDecimalValues, arrayOf({ v: Any? -> v }))
        )
    }

    @Test
    fun testMax() {
        assertEquals(BigDecimal(3), maxOfFunctionExecutor.extractFrom("maxOf", intValues, arrayOf(defaultPredicate)))
        assertEquals(BigDecimal("3.09"), maxOfFunctionExecutor.extractFrom("maxOf", doubleValues, arrayOf(defaultPredicate)))
        assertEquals(BigDecimal("15.1"), maxOfFunctionExecutor.extractFrom("maxOf", stringValues, arrayOf(defaultPredicate)))
        assertEquals(
            BigDecimal(100), maxOfFunctionExecutor.extractFrom("maxOf", bigDecimalValues, arrayOf({ v: Any? -> v }))
        )
    }
}