package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.exception.TestValueConversionException
import java.nio.file.Files
import java.nio.file.Paths

@DisplayName("Convert to file content")
class FileFunctionExecutorTest {

    private var fileFunctionExecutor: FileFunctionExecutor? = null

    @BeforeEach
    fun setUp() {
        fileFunctionExecutor = FileFunctionExecutor(javaClass.classLoader)
    }

    @Test
    @DisplayName("When file exist Then check content is returned")
    fun testValidatePathToJsonFile() {
        val filePath = "csv/test-file.csv"

        val expectedContent = readFromFileExpectedResult("/$filePath")

        Assertions.assertEquals(expectedContent, fileFunctionExecutor!!.execute("fromFile", arrayOf(filePath)))
    }

    @Test
    @DisplayName("When file doesn't exist Then throw exception")
    fun testFilePathIsEmpty() {
        val filePath = "invalid"

        val exception = Assertions.assertThrows(TestValueConversionException::class.java) {
            fileFunctionExecutor!!.execute("fromFile", arrayOf(filePath))
        }

        Assertions.assertEquals("File '$filePath' doesn't exist", exception.message)
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