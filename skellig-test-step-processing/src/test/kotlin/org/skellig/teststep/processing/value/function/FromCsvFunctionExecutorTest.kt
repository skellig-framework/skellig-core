package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.value.function.exception.FunctionValueExecutionException

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
        val result = fromCsvFunctionExecutor!!.execute("fromCsv", arrayOf("csv/test-file.csv"))

        Assertions.assertAll(
            { Assertions.assertEquals("1", ((result as List<*>?)!![0] as Map<*, *>)["id"]) },
            { Assertions.assertEquals("n1", ((result as List<*>?)!![0] as Map<*, *>)["name"]) },
            { Assertions.assertEquals("v1", ((result as List<*>?)!![0] as Map<*, *>)["value"]) },
            { Assertions.assertEquals("2", ((result as List<*>?)!![1] as Map<*, *>)["id"]) },
            { Assertions.assertEquals("n2", ((result as List<*>?)!![1] as Map<*, *>)["name"]) },
            { Assertions.assertEquals("v2", ((result as List<*>?)!![1] as Map<*, *>)["value"]) },
            { Assertions.assertEquals("3", ((result as List<*>?)!![2] as Map<*, *>)["id"]) },
            { Assertions.assertEquals("n3", ((result as List<*>?)!![2] as Map<*, *>)["name"]) },
            { Assertions.assertEquals("v3", ((result as List<*>?)!![2] as Map<*, *>)["value"]) }
        )
    }

    @Test
    @DisplayName("With row filter Then check filtered raws read")
    fun testConvertFromCsvFileWithFiltering() {

        val result = fromCsvFunctionExecutor!!.execute(
            "fromCsv",
            arrayOf("csv/test-file.csv", mapOf(Pair("id", "2"), Pair("name", "n2")))
        )

        Assertions.assertAll(
            { Assertions.assertEquals(1, (result as List<*>?)!!.size) },
            { Assertions.assertEquals("2", ((result as List<*>?)!![0] as Map<*, *>)["id"]) },
            { Assertions.assertEquals("n2", ((result as List<*>?)!![0] as Map<*, *>)["name"]) },
            { Assertions.assertEquals("v2", ((result as List<*>?)!![0] as Map<*, *>)["value"]) }
        )
    }

    @Test
    @DisplayName("When file does not exist Then throw exception")
    fun testConvertWhenFileNotExist() {
        Assertions.assertThrows(FunctionValueExecutionException::class.java) {
            fromCsvFunctionExecutor!!.execute("fromCsv", arrayOf("csv/missing.csv"))
        }
    }
}