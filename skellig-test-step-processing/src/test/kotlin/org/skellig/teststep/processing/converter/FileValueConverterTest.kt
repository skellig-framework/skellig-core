package org.skellig.teststep.processing.converter

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.exception.TestValueConversionException
import java.nio.file.Files
import java.nio.file.Paths

@DisplayName("Convert to file content")
class FileValueConverterTest {

    private var fileValueConverter: FileValueConverter? = null

    @BeforeEach
    fun setUp() {
        fileValueConverter = FileValueConverter(javaClass.classLoader)
    }

    @Test
    @DisplayName("When file exist Then check content is returned")
    fun testValidatePathToJsonFile() {
        val filePath = "csv/test-file.csv"

        val expectedContent = readFromFileExpectedResult("/$filePath")

        Assertions.assertEquals(expectedContent, fileValueConverter!!.convert(String.format("file(%s)", filePath)))
    }

    @Test
    @DisplayName("When file doesn't exist Then throw exception")
    fun testFilePathIsEmpty() {
        val filePath = "file(invalid)"

        val exception = Assertions.assertThrows(TestValueConversionException::class.java) { fileValueConverter!!.convert(filePath) }

        Assertions.assertEquals("File 'invalid' doesn't exist", exception.message)
    }

    private fun readFromFileExpectedResult(pathToFile: String): String? {
        return try {
            val path = Paths.get(javaClass.getResource(pathToFile).toURI())
            String(Files.readAllBytes(path))
        } catch (e: Exception) {
            null
        }
    }
}