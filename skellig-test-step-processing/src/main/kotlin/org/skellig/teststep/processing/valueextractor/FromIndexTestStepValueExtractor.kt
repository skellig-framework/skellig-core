package org.skellig.teststep.processing.valueextractor

import org.skellig.teststep.processing.exception.ValueExtractionException
import java.lang.reflect.Array

class FromIndexTestStepValueExtractor : TestStepValueExtractor {

    override fun extract(value: Any?, extractionParameter: String?): Any? {
        value?.let {
            val index = extractionParameter?.toInt() ?: error("Index cannot be null")
            return if (value.javaClass.isArray) {
                Array.get(value, index)
            } else {
                (value as List<*>)[index]
            }
        } ?: throw ValueExtractionException("Cannot extract '$extractionParameter' from null value")
    }

    override fun getExtractFunctionName(): String {
        return "fromIndex"
    }
}