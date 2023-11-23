package org.skellig.teststep.processing.value.extractor

import org.skellig.teststep.processing.exception.ValueExtractionException

class ValuesValueExtractor : ValueExtractor {

    override fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any? {
        value?.let {
            return when(value) {
                is Collection<*>, is Array<*> -> value
                is Map<*, *> -> value.values
                else -> value
            }
        } ?: throw ValueExtractionException("Cannot get values from null value")
    }

    override fun getExtractFunctionName(): String {
        return "values"
    }
}