package org.skellig.teststep.processing.value.function

import org.skellig.teststep.processing.value.exception.FunctionExecutionException

class SizeFunctionExecutor : FunctionValueExecutor {

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any? {
        value?.let {
            return when(value) {
                is Collection<*> -> value.size
                is Array<*> -> value.size
                is Map<*, *> -> value.size
                is String -> value.length
                else -> value
            }
        } ?: throw FunctionExecutionException("Cannot get values from null value")
    }

    override fun getFunctionName(): String {
        return "size"
    }
}