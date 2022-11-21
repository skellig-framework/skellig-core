package org.skellig.teststep.processing.valueextractor

import org.skellig.teststep.processing.exception.ValueExtractionException
import org.skellig.teststep.processing.experiment.ValueExtractor

class FromIndexTestStepValueExtractor : ValueExtractor {

    override fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any? {
        value?.let {
            if (args.size == 1) {
                val index = args[0]?.toString()?.toInt() ?: error("Index cannot be null")
                return if (value.javaClass.isArray) (value as Array<*>)[index]
                else (value as List<*>)[index]
            } else throw ValueExtractionException("fromIndex function can accept only 1 argument. Found: ${args.size}")
        } ?: throw ValueExtractionException("Cannot extract '${args[0]}' from null value")
    }

    override fun getExtractFunctionName(): String {
        return "fromIndex"
    }
}