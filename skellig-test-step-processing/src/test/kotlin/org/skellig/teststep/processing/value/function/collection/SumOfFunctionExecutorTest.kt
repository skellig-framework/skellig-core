package org.skellig.teststep.processing.value.function.collection

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class SumOfFunctionExecutorTest {

    private val functionExecutor = SumOfFunctionExecutor()

    @Test
    fun testSumExpression() {
        assertEquals(BigDecimal(4), functionExecutor.execute("", listOf(mapOf(Pair("price", 1)), mapOf(Pair("price", 3))), arrayOf({ v: Any? -> (v as Map<*, *>)["price"] })))
        assertEquals(
            BigDecimal("5.095"), functionExecutor.execute(
                "sumOf", listOf(mapOf(Pair("price", 3.09)), mapOf(Pair("price", 1)), mapOf(Pair("price", 1.005))), arrayOf({ v: Any? -> (v as Map<*, *>)["price"] })
            )
        )
        assertEquals(
            BigDecimal("35.0"), functionExecutor.execute(
                "sumOf",
                listOf(
                    mapOf(Pair("price", 15.7), Pair("delta", 2.0)),
                    mapOf(Pair("price", 1.0), Pair("delta", 3.6))
                ),
                arrayOf({ v: Any? -> ((v as Map<*, *>)["price"] as Double) * (v["delta"] as Double) })
            )
        )

        assertEquals(
            BigDecimal("6"), functionExecutor.execute(
                "sumOf",
                listOf("1", "2", "3"),
                arrayOf({ v: Any? -> v })
            )
        )
    }
}