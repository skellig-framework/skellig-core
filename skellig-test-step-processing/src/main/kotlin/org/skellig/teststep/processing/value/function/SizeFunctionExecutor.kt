package org.skellig.teststep.processing.value.function

import org.skellig.teststep.processing.value.exception.FunctionExecutionException

/**
 * This function returns the size of the given 'value', which can be a [Collection], [Array], [Map], or [String].
 * If the value is null or not one of those types, then a [FunctionExecutionException] is thrown.
 *
 * Supported args:
 * - size()
 */
class SizeFunctionExecutor : FunctionValueExecutor {

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any {
        value?.let {
            return when(value) {
                is Collection<*> -> value.size
                is Array<*> -> value.size
                is Map<*, *> -> value.size
                is String -> value.length
                else -> throw FunctionExecutionException("Value is invalid type for function '${getFunctionName()}'")
            }
        } ?: throw FunctionExecutionException("Function '${getFunctionName()}' cannot be called from null value")
    }

    override fun getFunctionName(): String {
        return "size"
    }
}