package org.skellig.teststep.processing.converter

import org.junit.Ignore
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.skellig.teststep.processing.exception.TestDataConversionException
import org.skellig.teststep.processing.utils.UnitTestUtils
import java.util.regex.Pattern

@Ignore
@DisplayName("Convert test data from ftl")
class TestDataFromFTLConverterTest {

    private var testDataFromFTLConverter: TestDataFromFTLConverter? = null

    @BeforeEach
    fun setUp() {
        val classLoader = javaClass.classLoader
        testDataFromFTLConverter = TestDataFromFTLConverter(classLoader, TestDataFromCsvConverter(classLoader))
    }

    @Test
    @DisplayName("When file and simple data model provided")
    fun testFtlConversion() {
        val templateDetails = UnitTestUtils.createMap(
            "template",
            UnitTestUtils.createMap(
                "file", "template/test.ftl",
                "name", "n1",
                "value", "v1"
            )
        )

        val result = testDataFromFTLConverter!!.convert(templateDetails)

        Assertions.assertEquals("""{ "name" : "n1" "value" : "v1"}""", inlineString(result))
    }

    @Test
    @DisplayName("When file and csv data model provided with row filter Then check correct row applied")
    fun testFtlConversionWithCsvDataModel() {
        val templateDetails = UnitTestUtils.createMap(
            "template",
            UnitTestUtils.createMap(
                "file", "template/test.ftl",
                "csv", UnitTestUtils.createMap(
                    "file", "csv/test-file.csv",
                    "row", UnitTestUtils.createMap("id", "3")
                )
            )
        )

        val result = testDataFromFTLConverter!!.convert(templateDetails)

        Assertions.assertEquals("""{ "name" : "n3" "value" : "v3"}""", inlineString(result))
    }

    @Test
    @DisplayName("When file and csv data model provided without filter Then check first row from csv applied")
    fun testFtlConversionWithCsvDataModelWithoutFilter() {
        val templateDetails = UnitTestUtils.createMap(
            "template",
            UnitTestUtils.createMap(
                "file", "template/test.ftl",
                "csv", UnitTestUtils.createMap("file", "csv/test-file.csv")
            )
        )

        val result = testDataFromFTLConverter!!.convert(templateDetails)

        Assertions.assertEquals("""{ "name" : "n1" "value" : "v1"}""", inlineString(result))
    }

    @Test
    @DisplayName("When file and csv data model provided without filter Then check first row from csv applied")
    fun testFtlConversionWhenFileNotExist() {
        val templateDetails = UnitTestUtils.createMap(
            "template",
            UnitTestUtils.createMap(
                "file", "template/invalid.ftl"
            )
        )

        Assertions.assertThrows(TestDataConversionException::class.java) {
            testDataFromFTLConverter!!.convert(
                templateDetails
            )
        }
    }

    private fun inlineString(result: Any?) = result.toString().replace(Regex("[\r\n]+"), "")
}