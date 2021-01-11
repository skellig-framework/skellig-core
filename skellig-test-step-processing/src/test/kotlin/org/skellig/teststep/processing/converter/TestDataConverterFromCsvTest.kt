package org.skellig.teststep.processing.converter

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.exception.TestDataConversionException
import org.skellig.teststep.processing.utils.UnitTestUtils

@DisplayName("Convert csv test data")
class TestDataConverterFromCsvTest {

    private var testDataFromCsvConverter: TestDataFromCsvConverter? = null

    @BeforeEach
    fun setUp() {
        testDataFromCsvConverter = TestDataFromCsvConverter(javaClass.classLoader)
    }

    @Test
    @DisplayName("Without row filter Then check all raws read")
    fun testConvertFromCsvFile() {
        val csvDetails = UnitTestUtils.createMap("csv",
                UnitTestUtils.createMap("file", "csv/test-file.csv"))

        val result = testDataFromCsvConverter!!.convert(csvDetails)

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
        val csvDetails = UnitTestUtils.createMap("csv",
                UnitTestUtils.createMap(
                        "file", "csv/test-file.csv",
                        "row",
                        UnitTestUtils.createMap("id", "2", "name", "n2"))
        )

        val result = testDataFromCsvConverter!!.convert(csvDetails)

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
        val csvDetails = UnitTestUtils.createMap("csv", UnitTestUtils.createMap("file", "csv/missing.csv"))

        Assertions.assertThrows(TestDataConversionException::class.java) { testDataFromCsvConverter!!.convert(csvDetails) }
    }
}