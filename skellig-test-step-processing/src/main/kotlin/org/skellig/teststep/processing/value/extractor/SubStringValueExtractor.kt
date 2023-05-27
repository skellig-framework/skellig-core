package org.skellig.teststep.processing.value.extractor

import org.skellig.teststep.processing.value.function.exception.FunctionValueExecutionException
import org.skellig.teststep.processing.value.extractor.exception.ValueExtractionException
import java.lang.String.format

open class SubStringValueExtractor : ValueExtractor {

    override fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any? {
        if (args.size == 1) {
            val extractionParameter = args[0]
            return value?.let {
                var newValue: String = value as String
                extractionParameter?.let {
                    newValue = subStringAfter(newValue, it.toString())
                }
                return newValue
            } ?: throw ValueExtractionException(format("Cannot extract sub string '%s' from null value", extractionParameter))
        } else {
            throw FunctionValueExecutionException("Function `subString` can only accept 1 String argument. Found ${args.size}")
        }
    }

    protected open fun subStringAfter(value: String, after: String): String {
        return value.substringAfter(after)
    }

    override fun getExtractFunctionName(): String {
        return "subString"
    }
}