package org.skellig.teststep.processing.value.function

import org.skellig.teststep.processing.exception.TestDataConversionException
import org.skellig.teststep.processing.exception.TestValueConversionException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

class FileFunctionExecutor(val classLoader: ClassLoader) : FunctionValueExecutor {

    override fun execute(name: String, args: Array<Any?>): Any =
        if (args.size == 1) {
            readFileContentFromFilePath(args[0]?.toString() ?: "")
        } else {
            throw TestDataConversionException("Function `fromFile` can only accept 1 String argument. Found ${args.size}")
        }

    private fun readFileContentFromFilePath(pathToFile: String): String {
        val resource = classLoader.getResource(pathToFile)
        return if (resource != null) {
            try {
                String(Files.readAllBytes(Paths.get(resource.toURI())), StandardCharsets.UTF_8)
            } catch (e: Exception) {
                throw TestValueConversionException(String.format("Failed to read file '%s'", pathToFile), e)
            }
        } else {
            throw TestValueConversionException(String.format("File '%s' doesn't exist", pathToFile))
        }
    }

    override fun getFunctionName(): String = "fromFile"
}