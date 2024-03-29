package org.skellig.teststep.processing.value.function.collection

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class FindFunctionExecutorTest {

    private val findFunctionExecutor = FindFunctionExecutor()
    private val findLastFunctionExecutor = FindLastFunctionExecutor()
    private val findAllFunctionExecutor = FindAllFunctionExecutor()
    private val values = listOf(
        mapOf(Pair("name", "Alex"), Pair("balance", 700)), mapOf(Pair("name", "Bob"), Pair("balance", 12)), mapOf(Pair("name", "Chuck"), Pair("balance", 45))
    )

    @Test
    fun testFindExpression() {
        assertEquals(values[1], findFunctionExecutor.execute("find", values, arrayOf({ v: Any? -> (v as Map<*, *>)["name"] == "Bob" })))
        assertNull(findFunctionExecutor.execute("find", values, arrayOf({ v: Any? -> (v as Map<*, *>)["name"] == "MyName" })))
    }

    @Test
    fun testFindLastExpression() {
        assertEquals(values[2], findLastFunctionExecutor.execute("findLast", values, arrayOf({ v: Any? -> ((v as Map<*, *>)["balance"] as Int) < 100 })))
        assertNull(findLastFunctionExecutor.execute("findLast", values, arrayOf({ v: Any? -> ((v as Map<*, *>)["balance"] as Int) > 1000 })))
    }

    @Test
    fun testFindAllExpression() {
        assertEquals(
            listOf(values[1], values[2]),
            findAllFunctionExecutor.execute("findLast", values, arrayOf({ v: Any? -> ((v as Map<*, *>)["balance"] as Int) < 100 }))
        )
        assertEquals(emptyList<Map<*, *>>(),
            findAllFunctionExecutor.execute("findLast", values, arrayOf({ v: Any? -> ((v as Map<*, *>)["balance"] as Int) > 1000 })))
    }
}