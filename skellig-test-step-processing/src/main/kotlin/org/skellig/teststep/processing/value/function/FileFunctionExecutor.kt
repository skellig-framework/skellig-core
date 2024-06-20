package org.skellig.teststep.processing.value.function

import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Represents a function executor that reads the content of a file from the given file path and returns it.
 *
 * Supported args:
 * - fromFile(`<path>`) - for example: fromFile(/tmp/file.log) will read a file 'file.log' and returns its content as [String].
 *
 */
class FileFunctionExecutor(val classLoader: ClassLoader) : FunctionValueExecutor {

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any =
        if (args.size == 1) {
            readFileContentFromFilePath(args[0]?.toString() ?: "")
        } else {
            throw FunctionExecutionException("Function `${getFunctionName()}` can only accept 1 String argument. Found ${args.size}")
        }

    private fun readFileContentFromFilePath(pathToFile: String): String {
        val resource = classLoader.getResource(pathToFile)
        return if (resource != null) {
            try {
                String(Files.readAllBytes(Paths.get(resource.toURI())), StandardCharsets.UTF_8)
            } catch (e: Exception) {
                throw FunctionExecutionException("Failed to read file '$pathToFile'", e)
            }
        } else {
            throw FunctionExecutionException("File '$pathToFile' doesn't exist")
        }
    }

    override fun getFunctionName(): String = "fromFile"
}