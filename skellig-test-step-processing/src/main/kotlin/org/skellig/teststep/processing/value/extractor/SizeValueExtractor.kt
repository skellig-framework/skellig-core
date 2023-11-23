package org.skellig.teststep.processing.value.extractor

import org.skellig.teststep.processing.exception.ValueExtractionException

class SizeValueExtractor : ValueExtractor {

    override fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any? {
        value?.let {
            return when(value) {
                is Collection<*> -> value.size
                is Array<*> -> value.size
                is Map<*, *> -> value.size
                is String -> value.length
                else -> value
            }
        } ?: throw ValueExtractionException("Cannot get values from null value")
    }

    override fun getExtractFunctionName(): String {
        return "size"
    }
}