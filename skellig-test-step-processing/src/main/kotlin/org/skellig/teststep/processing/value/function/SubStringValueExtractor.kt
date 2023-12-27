package org.skellig.teststep.processing.value.function

import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import java.lang.String.format

open class SubStringValueExtractor : FunctionValueExecutor {

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any? {
        if (args.size == 1) {
            val extractionParameter = args[0]
            return value?.let {
                var newValue: String = value as String
                extractionParameter?.let {
                    newValue = subStringAfter(newValue, it.toString())
                }
                return newValue
            } ?: throw FunctionExecutionException(format("Cannot extract sub string '%s' from null value", extractionParameter))
        } else {
            throw FunctionExecutionException("Function `subString` can only accept 1 String argument. Found ${args.size}")
        }
    }

    protected open fun subStringAfter(value: String, after: String): String {
        return value.substringAfter(after)
    }

    override fun getFunctionName(): String {
        return "subString"
    }
}