package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import java.time.LocalDate
import java.time.LocalDateTime

@DisplayName("convert to json")
internal class ToJsonFunctionExecutorTest {

    private val converter = ToJsonFunctionExecutor()

    @Test
    fun `from empty map`() {
        assertEquals("{}", converter.execute("toJson", null, arrayOf(emptyMap<Any, Any>())))
    }

    @Test
    fun `from map`() {
        val testData = mapOf<Any, Any>(Pair("f1", "v1"), Pair("f2", 2))

        assertEquals("{\"f1\":\"v1\",\"f2\":2}", converter.execute("toJson", null, arrayOf(testData)))
    }

    @Test
    fun `from map with date`() {
        val date = LocalDate.of(2020, 2, 2)
        val time = LocalDateTime.of(2020, 1, 1, 10, 20, 30)
        val testData = mapOf(Pair("f1", date), Pair("f2", time))

        assertEquals("""{"f1":"2020-02-02","f2":"2020-01-01T10:20:30"}""", converter.execute("toJson", null, arrayOf(testData)))
    }

    @Test
    fun `from complex object`() {
        val testData = mapOf(Pair("f1", listOf(mapOf(Pair("a", "b")))), Pair("f2", mapOf(Pair("c", "d"))))

        assertEquals("{\"f1\":[{\"a\":\"b\"}],\"f2\":{\"c\":\"d\"}}", converter.execute("toJson", null, arrayOf(testData)))
    }

    @Test
    fun `from list`() {
        val testData = listOf("a", "b", "c")

        assertEquals("[\"a\",\"b\",\"c\"]", converter.execute("toJson", null, arrayOf(testData)))
    }

    @Test
    fun `from object`() {
        assertEquals("{\"id\":100,\"name\":\"n1\"}", converter.execute("toJson", null, arrayOf(ConvertableDataForTest(100, "n1"))))
    }

    @Test
    fun `when invalid number of args provided`() {
        val ex = Assertions.assertThrows(FunctionExecutionException::class.java) { converter.execute("toJson", null, arrayOf(emptyMap<Any, Any>(), 10)) }

        assertEquals("Function `toJson` can only accept 1 argument. Found 2", ex.message)
    }

    @Test
    fun `from provided value as object`() {
        assertEquals("{\"id\":100,\"name\":\"n1\"}", converter.execute("toJson", ConvertableDataForTest(100, "n1"), emptyArray()))
    }

    @Test
    fun `when argument has null value`() {
        assertNull(converter.execute("toJson", null, arrayOf(null)))
    }

    inner class ConvertableDataForTest(var id: Int, var name: String)
}