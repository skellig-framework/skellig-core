package org.skellig.teststep.processing.valueextractor

import org.skellig.teststep.processing.exception.ValueExtractionException
import java.lang.String.format
import java.util.regex.Matcher
import java.util.regex.Pattern

class RegexTestStepValueExtractor : TestStepValueExtractor {

    override fun extract(value: Any?, extractionParameter: String?): Any? {
        return value?.let {
            val matcher = Pattern.compile(getExtractionParameter(extractionParameter ?: "")).matcher(it as String)
            val result = extractGroups(matcher)
            return when {
                result.size == 1 -> result.first()
                result.size > 1 -> result
                else -> value
            }
        } ?: throw ValueExtractionException(format("Cannot extract '%s' from null value", extractionParameter))
    }

    private fun getExtractionParameter(extractionParameter: String) : String {
        if (extractionParameter.length > 2) {
            val firstChar = extractionParameter[0]
            val lastChar = extractionParameter[extractionParameter.length - 1]
            if ((firstChar == '\'' && lastChar == '\'') || (firstChar == '\"' && lastChar == '\"'))
                return extractionParameter.substring(1, extractionParameter.length - 1)
        }
        return extractionParameter
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
        return "regex"
    }
}