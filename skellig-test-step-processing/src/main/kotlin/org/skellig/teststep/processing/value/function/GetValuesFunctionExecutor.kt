package org.skellig.teststep.processing.value.function

import org.skellig.teststep.processing.value.exception.FunctionExecutionException

/**
 * Execute the "getValues" function on [Collection] or [Map] 'value', but otherwise returns the same 'value' wrapped in [List].
 *
 * Supported args:
 * - getValues()
 */
class GetValuesFunctionExecutor : FunctionValueExecutor {

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any {
        value?.let {
            return when(value) {
                is Collection<*>, is Array<*> -> value
                is Map<*, *> -> value.values
                else -> listOf(value)
            }
        } ?: throw FunctionExecutionException("Cannot get values from null value")
    }

    override fun getFunctionName(): String {
        return "getValues"
    }
}