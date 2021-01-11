package org.skellig.teststep.processing.valueextractor

import org.skellig.teststep.processing.exception.ValueExtractionException
import java.lang.String.format

open class SubStringTestStepValueExtractor : TestStepValueExtractor {

    override fun extract(value: Any?, extractionParameter: String?): Any {
        return value?.let {
            var newValue: String = value as String
            extractionParameter?.split(",")?.let {
                newValue = subStringAfter(newValue, it[0])
                if (it.size > 1 && it[1].isNotEmpty()) {
                    newValue = newValue.substringBefore(it[1])
                }
            }
            return newValue
        } ?: throw ValueExtractionException(format("Cannot extract sub string '%s' from null value", extractionParameter))
    }

    protected open fun subStringAfter(value: String, after: String): String {
        return value.substringAfter(after)
    }

    override fun getExtractFunctionName(): String {
        return "subString"
    }
}