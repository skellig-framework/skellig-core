package org.skellig.teststep.processing.converter

import org.apache.commons.lang3.StringUtils
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*
import java.util.regex.Pattern

class IncrementValueConverter : TestStepValueConverter {

    companion object {
        private val NAMED_INCREMENT_REGEX = Pattern.compile("inc\\((.*)\\)")
        private val SPLIT_COMMA_REGEX = Pattern.compile(",")
        private val SPLIT_SPACE_REGEX = Pattern.compile(" ")
        const val DEFAULT_INC_NAME = "skellig_default"
        const val FILE_NAME = "skellig-inc.tmp"
    }

    override fun convert(value: Any?): Any? =
            value?.let {
                val matcher = NAMED_INCREMENT_REGEX.matcher(value.toString())
                var result = value
                if (matcher.find()) {
                    var maxLength = 1
                    var key: String? = null
                    val params = SPLIT_COMMA_REGEX.split(matcher.group(1))
                    if (params.size == 2) {
                        key = params[0].trim { it <= ' ' }
                        maxLength = params[1].trim { it <= ' ' }.toInt()
                    } else if (params.size == 1) {
                        val firstParameter = params[0].trim { it <= ' ' }
                        when {
                            isNumber(firstParameter) -> {
                                maxLength = firstParameter.toInt()
                            }
                            firstParameter.isNotEmpty() -> {
                                key = firstParameter
                            }
                            else -> {
                                key = DEFAULT_INC_NAME
                            }
                        }
                    }
                    val currentValue = getCurrentValue(key, maxLength)
                    result = incrementAndGet(currentValue ?: getDefaultValue(maxLength),
                            if (maxLength == 1 && currentValue != null) currentValue.length else maxLength)
                    replaceOldValueInFile(key, result, currentValue != null)
                }
                result
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
        return line.isNotEmpty() && line[0].toInt() >= 48 && line[0].toInt() <= 57
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
        } catch (ex: Exception) {
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
}