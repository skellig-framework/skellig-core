package org.skellig.teststep.processing.value.function.collection

import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import org.skellig.teststep.processing.value.function.FunctionValueExecutor


class FirstFunctionExecutor : FunctionValueExecutor {

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any? {
        value?.let {
            return when(value) {
                is Collection<*> -> value.first()
                is Array<*> -> value.first()
                else -> throw FunctionExecutionException("Function '${getFunctionName()}' can only be called from array or collection of items")
            }
        } ?: throw FunctionExecutionException("Cannot get first item from null value when calling function '${getFunctionName()}'")
    }

    override fun getFunctionName(): String = "first"

}