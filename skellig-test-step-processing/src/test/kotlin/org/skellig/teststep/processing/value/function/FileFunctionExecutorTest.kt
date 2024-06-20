package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.skellig.teststep.processing.value.exception.FunctionExecutionException
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
    fun testWhenFileExistWithContent() {
        val filePath = "csv/test-file.csv"

        val expectedContent = readFromFileExpectedResult("/$filePath")

        assertEquals(expectedContent, fileFunctionExecutor!!.execute("fromFile", null, arrayOf(filePath)))
    }

    @Test
    @DisplayName("When file exist Then check content is returned")
    fun testWhenNoPathProvided() {
        val ex = assertThrows<FunctionExecutionException> { fileFunctionExecutor!!.execute("fromFile", null, emptyArray()) }
        assertEquals("Function `fromFile` can only accept 1 String argument. Found 0", ex.message)
    }

    @Test
    @DisplayName("When file doesn't exist Then throw exception")
    fun testFilePathIsEmpty() {
        val filePath = "invalid"

        val exception = Assertions.assertThrows(FunctionExecutionException::class.java) {
            fileFunctionExecutor!!.execute("fromFile", null, arrayOf(filePath))
        }

        assertEquals("File '$filePath' doesn't exist", exception.message)
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