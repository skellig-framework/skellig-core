package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.value.exception.FunctionExecutionException

@DisplayName("Convert csv test data")
class FromCsvFunctionExecutorTest {

    private var fromCsvFunctionExecutor: FromCsvFunctionExecutor? = null

    @BeforeEach
    fun setUp() {
        fromCsvFunctionExecutor = FromCsvFunctionExecutor(javaClass.classLoader)
    }

    @Test
    @DisplayName("Without row filter Then check all raws read")
    fun testConvertFromCsvFile() {
        val result = fromCsvFunctionExecutor!!.execute("fromCsv", null, arrayOf("csv/test-file.csv"))

        Assertions.assertAll(
            { assertEquals("1", ((result as List<*>?)!![0] as Map<*, *>)["id"]) },
            { assertEquals("n1", ((result as List<*>?)!![0] as Map<*, *>)["name"]) },
            { assertEquals("v1", ((result as List<*>?)!![0] as Map<*, *>)["value"]) },
            { assertEquals("2", ((result as List<*>?)!![1] as Map<*, *>)["id"]) },
            { assertEquals("n2", ((result as List<*>?)!![1] as Map<*, *>)["name"]) },
            { assertEquals("v2", ((result as List<*>?)!![1] as Map<*, *>)["value"]) },
            { assertEquals("3", ((result as List<*>?)!![2] as Map<*, *>)["id"]) },
            { assertEquals("n3", ((result as List<*>?)!![2] as Map<*, *>)["name"]) },
            { assertEquals("v3", ((result as List<*>?)!![2] as Map<*, *>)["value"]) }
        )
    }

    @Test
    @DisplayName("With row filter Then check filtered raws read")
    fun testConvertFromCsvFileWithFiltering() {

        val result = fromCsvFunctionExecutor!!.execute(
            "fromCsv", null,
            arrayOf("csv/test-file.csv", mapOf(Pair("id", "2"), Pair("name", "n2")))
        )

        Assertions.assertAll(
            { assertEquals(1, (result as List<*>?)!!.size) },
            { assertEquals("2", ((result as List<*>?)!![0] as Map<*, *>)["id"]) },
            { assertEquals("n2", ((result as List<*>?)!![0] as Map<*, *>)["name"]) },
            { assertEquals("v2", ((result as List<*>?)!![0] as Map<*, *>)["value"]) }
        )
    }

    @Test
    @DisplayName("When file does not exist Then throw exception")
    fun testConvertWhenFileNotExist() {
        Assertions.assertThrows(FunctionExecutionException::class.java) {
            fromCsvFunctionExecutor!!.execute("fromCsv", null, arrayOf("csv/missing.csv"))
        }
    }

    @Test
    @DisplayName("When path to file not provided")
    fun testConvertWhenNoPathProvided() {
        val ex = Assertions.assertThrows(FunctionExecutionException::class.java) {
            fromCsvFunctionExecutor!!.execute("fromCsv", null, emptyArray())
        }
        assertEquals("Function `fromCsv` can only accept 1 or 2 arguments. Found 0", ex.message)
    }
}