package org.skellig.teststep.processing.value.function

import org.apache.commons.lang3.StringUtils
import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.regex.Pattern

/**
 * Class to execute the increment function 'inc' which increments a value based on a provided pattern, for example:
 *```
 * inc(id, 10)
 *```
 * returns 0000000001, then 0000000002, etc.
 *
 * The 'id' is used as any unique name to store the previous result and reuse it next increment.
 *
 * Supported args:
 * - inc(`<key>`, `<max length>`) - increments a number with `<max length>` and assigns it to `<key>`.
 * - inc(`<max length>`) - increments a number with `<max length>` and assigns it to `skellig_default` key.
 */
class IncrementFunctionExecutor : FunctionValueExecutor {

    companion object {
        private val SPLIT_SPACE_REGEX = Pattern.compile(" ")
        const val DEFAULT_INC_NAME = "skellig_default"
        const val FILE_NAME = "skellig-inc.tmp"
    }

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any {
        if (value != null) throw FunctionExecutionException("Function `${getFunctionName()}` cannot be called from another value")

        var maxLength = 1
        var key: String? = null
        if (args.size == 2) {
            key = args[0]?.toString()?.trim { it <= ' ' }
            maxLength = args[1]?.toString()?.toInt() ?: maxLength
        } else if (args.size == 1) {
            val firstParameter = args[0]?.toString() ?: ""
            when {
                isNumber(firstParameter) -> maxLength = firstParameter.toInt()
                firstParameter.isNotEmpty() -> key = firstParameter
                else -> key = DEFAULT_INC_NAME
            }
        }
        val currentValue = getCurrentValue(key, maxLength)
        val result = incrementAndGet(
            currentValue ?: getDefaultValue(maxLength),
            if (maxLength == 1 && currentValue != null) currentValue.length else maxLength
        )
        replaceOldValueInFile(key, result, currentValue != null)
        return result
    }

    private fun incrementAndGet(valueToIncrement: String, maxLength: Int): String {
        val incrementedValue = valueToIncrement.toInt() + 1
        val newLength = incrementedValue.toString().length
        return if (newLength <= maxLength) StringUtils.repeat("0", maxLength - newLength) + incrementedValue else valueToIncrement
    }

    private fun getDefaultValue(maxLength: Int): String {
        return StringUtils.repeat("0", maxLength)
    }

    private fun getCurrentValue(key: String?, maxLength: Int): String? {
        return try {
            val pathToFile = Paths.get(FILE_NAME)
            if (!Files.exists(pathToFile)) {
                Files.createFile(pathToFile)
            }
            File(pathToFile.toString()).readLines()
                .filter { line: String -> isMatchLine(key, maxLength, line) }
                .map { line: String -> if (key != null && line.startsWith(key)) SPLIT_SPACE_REGEX.split(line)[1] else line }
                .first()
        } catch (e: Exception) {
            null
        }
    }

    private fun isMatchLine(key: String?, maxLength: Int, line: String): Boolean {
        return key != null && line.startsWith(key) || isNumber(line) && line.length == maxLength
    }

    private fun isNumber(line: String): Boolean {
        return line.isNotEmpty() && line[0].code >= 48 && line[0].code <= 57
    }

    private fun replaceOldValueInFile(key: String?, newValue: String, isOldValue: Boolean) {
        try {
            val pathToFile = Paths.get(FILE_NAME)
            if (Files.exists(pathToFile)) {
                if (isOldValue) {
                    overwriteValueInFile(key, newValue, pathToFile)
                } else {
                    writeNewValueInFile(key, newValue, pathToFile)
                }
            }
        } catch (_: Exception) {
        }
    }

    @Throws(IOException::class)
    private fun writeNewValueInFile(key: String?, newValue: String, pathToFile: Path) {
        Files.write(pathToFile, listOf(if (key != null) "$key $newValue" else newValue), StandardOpenOption.APPEND)
    }

    @Throws(IOException::class)
    private fun overwriteValueInFile(key: String?, newValue: String, pathToFile: Path) {
        val newLines = File(pathToFile.toString()).readLines().map { line ->
            if (isMatchLine(key, newValue.length, line)) {
                return@map if (key != null && line.startsWith(key)) "$key $newValue" else newValue
            } else {
                return@map line
            }
        }
        Files.write(pathToFile, newLines)
    }

    override fun getFunctionName(): String = "inc"
}
