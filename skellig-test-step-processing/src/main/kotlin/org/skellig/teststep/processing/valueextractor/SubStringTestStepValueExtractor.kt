package org.skellig.teststep.processing.valueextractor

import org.skellig.teststep.processing.exception.TestDataConversionException
import org.skellig.teststep.processing.exception.ValueExtractionException
import org.skellig.teststep.processing.experiment.ValueExtractor
import java.lang.String.format

open class SubStringTestStepValueExtractor : TestStepValueExtractor, ValueExtractor {

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
            throw TestDataConversionException("Function `subString` can only accept 1 String argument. Found ${args.size}")
        }
    }

    override fun extract(value: Any?, extractionParameter: String?): Any {
        return value?.let {
            var newValue: String = value as String
            extractionParameter?.let {
                newValue = subStringAfter(newValue, it)
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