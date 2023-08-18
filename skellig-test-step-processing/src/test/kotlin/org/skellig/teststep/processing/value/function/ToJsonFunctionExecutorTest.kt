package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.value.function.exception.FunctionValueExecutionException
import java.time.LocalDate
import java.time.LocalDateTime

internal class ToJsonFunctionExecutorTest {

    private val converter = ToJsonFunctionExecutor()

    @Test
    internal fun testConvertToEmptyMapJson() {
        assertEquals("{}", converter.execute("toJson", arrayOf(emptyMap<Any, Any>())))
    }

    @Test
    internal fun testConvertToSimpleJson() {
        val testData = mapOf<Any, Any>(Pair("f1", "v1"), Pair("f2", 2))

        assertEquals("{\"f1\":\"v1\",\"f2\":2}", converter.execute("toJson", arrayOf(testData)))
    }

    @Test
    internal fun testConvertToJsonWithDate() {
        val date = LocalDate.of(2020, 2, 2)
        val time = LocalDateTime.of(2020, 1, 1, 10, 20, 30)
        val testData = mapOf(Pair("f1", date), Pair("f2", time))

        assertEquals("""{"f1":"2020-02-02","f2":"2020-01-01T10:20:30"}""", converter.execute("toJson", arrayOf(testData)))
    }

    @Test
    internal fun testConvertToComplexJson() {
        val testData = mapOf(Pair("f1", listOf(mapOf(Pair("a", "b")))), Pair("f2", mapOf(Pair("c", "d"))))

        assertEquals("{\"f1\":[{\"a\":\"b\"}],\"f2\":{\"c\":\"d\"}}", converter.execute("toJson", arrayOf(testData)))
    }

    @Test
    internal fun testConvertToListJson() {
        val testData = listOf("a", "b", "c")

        assertEquals("[\"a\",\"b\",\"c\"]", converter.execute("toJson", arrayOf(testData)))
    }

    @Test
    internal fun testConvertToObjectJson() {
        assertEquals("{\"id\":100,\"name\":\"n1\"}", converter.execute("toJson", arrayOf(ConvertableDataForTest(100, "n1"))))
    }

    @Test
    fun testConvertToJsonWithInvalidNumberOfArguments() {
        val ex = Assertions.assertThrows(FunctionValueExecutionException::class.java) { converter.execute("toJson", arrayOf(emptyMap<Any, Any>(), 10)) }

        assertEquals("Function `toJson` can only accept 1 argument. Found 2", ex.message)
    }

    inner class ConvertableDataForTest(var id: Int, var name: String)
}