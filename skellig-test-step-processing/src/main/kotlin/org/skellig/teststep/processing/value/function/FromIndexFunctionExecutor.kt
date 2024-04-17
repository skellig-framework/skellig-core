package org.skellig.teststep.processing.value.function

import org.skellig.teststep.processing.value.exception.FunctionExecutionException

/**
 * Executes the "fromIndex" function, which returns the value at the specified index in an array or list.
 * This is usually applied in a call chain where previous value is another function, for example:
 *```
 * values().fromIndex(0)
 *```
 *
 * Supported args:
 * - fromIndex(`<index>`) - for example: fromIndex(1), which returns an item in index 1 from the 'value'. Index can be [Int] or [String] type.
 */
class FromIndexFunctionExecutor : FunctionValueExecutor {

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