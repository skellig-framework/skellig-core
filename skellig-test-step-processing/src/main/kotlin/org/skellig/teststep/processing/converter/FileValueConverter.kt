package org.skellig.teststep.processing.converter

import org.skellig.teststep.processing.exception.TestValueConversionException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.regex.Pattern

class FileValueConverter(val classLoader: ClassLoader) : TestStepValueConverter {

    companion object {
        private val FILE_PATTERN = Pattern.compile("fromFile\\((.+)\\)")
    }

    override fun convert(value: Any?): Any? =
            when (value) {
                is String -> {
                    val matcher = FILE_PATTERN.matcher(value.toString())
                    if (matcher.find()) {
                        readFileContentFromFilePath(matcher.group(1))
                    } else value
                }
                else -> value
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
}