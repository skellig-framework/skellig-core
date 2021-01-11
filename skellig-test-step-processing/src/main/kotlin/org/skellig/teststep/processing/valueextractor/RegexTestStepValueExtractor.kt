package org.skellig.teststep.processing.valueextractor

import org.skellig.teststep.processing.exception.ValueExtractionException
import java.lang.String.format
import java.util.regex.Pattern

class RegexTestStepValueExtractor : TestStepValueExtractor {

    override fun extract(value: Any?, extractionParameter: String?): Any? {
        return value?.let {
            val matcher = Pattern.compile(extractionParameter!!).matcher(it as String)
            return if (matcher.find()) {
                matcher.group(1)
            } else {
                value
            }
        } ?: throw ValueExtractionException(format("Cannot extract '%s' from null value", extractionParameter))
    }

    override fun getExtractFunctionName(): String? {
        return "regex"
    }
}