package org.skellig.teststep.processing.value.function

import org.skellig.teststep.processing.value.exception.FunctionExecutionException

class FromIndexValueExtractor : FunctionValueExecutor {

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any? {
        value?.let {
            if (args.size == 1) {
                val index = args[0]?.toString()?.toInt() ?: error("Index cannot be null")
                return if (value.javaClass.isArray) (value as Array<*>)[index]
                else (value as List<*>)[index]
            } else throw FunctionExecutionException("fromIndex function can accept only 1 argument. Found: ${args.size}")
        } ?: throw FunctionExecutionException("Cannot extract '${args[0]}' from null value")
    }

    override fun getFunctionName(): String {
        return "fromIndex"
    }
}