package org.skellig.teststep.processing.valueextractor

import org.skellig.teststep.processing.exception.TestDataConversionException
import org.skellig.teststep.processing.exception.ValueExtractionException
import org.skellig.teststep.processing.experiment.ValueExtractor
import java.lang.String.format
import java.util.regex.Matcher
import java.util.regex.Pattern

class RegexTestStepValueExtractor : ValueExtractor {

    override fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any {
        if (args.size == 1) {
            val extractionParameter = args[0]?.toString() ?: ""
            return value?.let {
                val matcher = Pattern.compile(extractionParameter).matcher(it as String)
                val result = extractGroups(matcher)
                return when {
                    result.size == 1 -> result.first()
                    result.size > 1 -> result
                    else -> value
                }
            } ?: throw ValueExtractionException(format("Cannot extract '%s' from null value", extractionParameter))
        } else {
            throw TestDataConversionException("Function `regex` can only accept 1 argument. Found ${args.size}")
        }
    }

    private fun extractGroups(matcher: Matcher): MutableList<String> {
        val result = mutableListOf<String>()
        while (matcher.find()) {
            for (i in (if (matcher.groupCount() == 0) 0 else 1)..matcher.groupCount()) {
                result.add(matcher.group(i))
            }
        }
        return result
    }

    override fun getExtractFunctionName(): String {
        return "fromRegex"
    }
}