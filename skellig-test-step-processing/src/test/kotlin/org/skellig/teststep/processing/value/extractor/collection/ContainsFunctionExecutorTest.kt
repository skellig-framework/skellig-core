package org.skellig.teststep.processing.value.extractor.collection

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import java.time.LocalDateTime
import java.time.LocalDateTime.now

class ContainsFunctionExecutorTest {

    private val anyFunctionExecutor = AnyFunctionExecutor()
    private val noneFunctionExecutor = NoneFunctionExecutor()
    private val allFunctionExecutor = AllFunctionExecutor()

    @Test
    fun testWhenNotCollection() {
        val ex = assertThrows<FunctionExecutionException> { anyFunctionExecutor.extractFrom("any", "any data", arrayOf({ v: Any? -> v != null })) }
        assertEquals("Cannot execute function 'any' on class java.lang.String as it's only allowed for a collection of items with an argument as a predicate", ex.message)
    }

    @Test
    fun testWhenInvalidArgType() {
        var ex = assertThrows<FunctionExecutionException> { anyFunctionExecutor.extractFrom("any", emptyList<Any>(), arrayOf()) }
        assertEquals("Failed to execute function 'any' as it must have 1 String argument but has 0", ex.message)

        ex = assertThrows<FunctionExecutionException> { anyFunctionExecutor.extractFrom("any", emptyList<Any>(), arrayOf("arg")) }
        assertEquals("Cannot execute function 'any' on class kotlin.collections.EmptyList as it's only allowed for a collection of items with an argument as a predicate", ex.message)
    }

    @Test
    fun testWhenPredicateDoesNotReturnBoolean() {
        val ex = assertThrows<FunctionExecutionException> { anyFunctionExecutor.extractFrom("any", listOf("data"), arrayOf({ _: Any? -> 0 })) }
        assertEquals(
            "Unexpected result type returned when executing a predicate of the function 'any'\n" +
                    "Expected 'boolean' but was 'class java.lang.Integer'", ex.message
        )
    }

    @Test
    fun testWhenFoundAny() {
        assertTrue(anyFunctionExecutor.extractFrom("any", listOf(mapOf(Pair("price", 22)), mapOf(Pair("price", 12))), arrayOf({ v: Any? -> (v as Map<*, *>)["price"] == 12 })) as Boolean)
        assertTrue(
            anyFunctionExecutor.extractFrom(
                "any", listOf(mapOf(Pair("price", 50.45)), mapOf(Pair("price", 14.002))), arrayOf({ v: Any? -> ((v as Map<*, *>)["price"] as Double) < 14.003 })
            ) as Boolean
        )
        assertTrue(
            anyFunctionExecutor.extractFrom(
                "any", listOf(mapOf(Pair("price", 14.002)), mapOf(Pair("price", 14.003))), arrayOf({ v: Any? -> ((v as Map<*, *>)["price"] as Double) >= 14.003 })
            ) as Boolean
        )
        assertFalse(anyFunctionExecutor.extractFrom("any", listOf(mapOf(Pair("price", 0))), arrayOf({ v: Any? -> ((v as Map<*, *>)["price"] as Int) > 0 })) as Boolean)
        assertTrue(anyFunctionExecutor.extractFrom("any", listOf(mapOf(Pair("name", "some"))), arrayOf({ v: Any? -> (v as Map<*, *>)["name"] != null })) as Boolean)
        assertTrue(
            anyFunctionExecutor.extractFrom(
                "any",
                listOf(mapOf(Pair("date", now().minusDays(1)))),
                arrayOf({ v: Any? -> ((v as Map<*, *>)["date"] as LocalDateTime).isBefore(now()) })
            ) as Boolean
        )
    }

    @Test
    fun testWhenFoundNone() {
        val value = listOf(mapOf(Pair("price", 22)), mapOf(Pair("price", 12)))
        assertTrue(noneFunctionExecutor.extractFrom("none", value, arrayOf({ v: Any? -> (v as Map<*, *>)["price"] == 500 })) as Boolean)
        assertFalse(noneFunctionExecutor.extractFrom("none", value, arrayOf({ v: Any? -> (v as Map<*, *>)["price"] == 12 })) as Boolean)
    }

    @Test
    fun testWhenFoundAll() {
        val value = listOf(mapOf(Pair("price", 22)), mapOf(Pair("price", 12)))
        assertTrue(allFunctionExecutor.extractFrom("none", value, arrayOf({ v: Any? -> ((v as Map<*, *>)["price"] as Int) > 0 })) as Boolean)
        assertFalse(allFunctionExecutor.extractFrom("none", value, arrayOf({ v: Any? -> (v as Map<*, *>)["price"] == 12 })) as Boolean)
    }
}