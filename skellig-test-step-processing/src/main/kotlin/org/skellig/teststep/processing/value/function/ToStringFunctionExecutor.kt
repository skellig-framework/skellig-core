package org.skellig.teststep.processing.value.function

import java.nio.charset.Charset

/**
 * Executes the toString function on a given value.
 *
 * Supported args:
 * - toString()
 */
class ToStringFunctionExecutor : FunctionValueExecutor {

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any {
        return value?.let {
            when (it) {
                is ByteArray -> String(it, Charset.forName(if(args.isEmpty()) "utf8" else args[0]?.toString()))
                else -> it.toString()
            }
        } ?: "null"
    }

    override fun getFunctionName(): String {
        return "toString"
    }
}