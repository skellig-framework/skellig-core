package org.skellig.teststep.processing.value.function.collection

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skellig.teststep.processing.value.exception.FunctionExecutionException

class AddFunctionExecutorTest {

    private val functionExecutor = AddFunctionExecutor()

    @Test
    fun testAddToCollection() {
        assertEquals(listOf("a", "b"), functionExecutor.execute("", listOf("a"), arrayOf("b")))
        assertEquals(listOf(1, 2, 3), functionExecutor.execute("", listOf<Int>(), arrayOf(1, 2, 3)))
        assertEquals(listOf("a", "b", "a"), functionExecutor.execute("", mutableListOf("a"), arrayOf("b", "a")))
        assertEquals(listOf("a", null), functionExecutor.execute("", mutableListOf("a"), arrayOf(null)))
        assertEquals(listOf("a", 1), functionExecutor.execute("", mutableListOf("a"), arrayOf(1)))
        assertEquals(setOf("a", "b"), functionExecutor.execute("", setOf("a"), arrayOf("b", "a")))
    }

    @Test
    fun testAddToMap() {
        assertEquals(
            mapOf(Pair("a", "0"), Pair("b", "1")),
            functionExecutor.execute("", mapOf(Pair("a", "0")), arrayOf(mapOf(Pair("b", "1"))))
        )
        assertEquals(
            mapOf(Pair("b", "1")),
            functionExecutor.execute("", mapOf<String, String>(), arrayOf(mapOf(Pair("b", "1"))))
        )
        assertEquals(
            mapOf(Pair("a", "0")),
            functionExecutor.execute("", mapOf(Pair("a", "0")), arrayOf(mapOf<String, String>()))
        )
        assertEquals(
            mapOf(Pair("a", "0"), Pair("b", 1), Pair(2, emptyList<String>())),
            functionExecutor.execute("", mapOf(Pair("a", "0")), arrayOf(mapOf(Pair("b", 1), Pair(2, emptyList<String>()))))
        )
    }

    @Test
    fun testAddToCollectionWithInvalidData() {
        val ex = assertThrows<FunctionExecutionException> { functionExecutor.execute("", listOf("a"), arrayOf()) }
        assertEquals("Failed to execute function 'add' as it must have at least 1 argument", ex.message)

        val ex2 = assertThrows<FunctionExecutionException> { functionExecutor.execute("", "a", arrayOf(1)) }
        assertEquals("The function 'add' can only accept Collection or Map as value. Found: class java.lang.String", ex2.message)
    }

    @Test
    fun testAddToMapWithInvalidData() {
        val ex = assertThrows<FunctionExecutionException> { functionExecutor.execute("", mapOf(Pair("a", "0")), arrayOf()) }
        assertEquals("Failed to execute function 'add' as it must have at least 1 argument", ex.message)

        val ex2 = assertThrows<FunctionExecutionException> { functionExecutor.execute("", mapOf(Pair("a", "0")), arrayOf("1")) }
        assertEquals("The function 'add' of Map value can only accept Map as an arguments. Found: class java.lang.String", ex2.message)
    }
}