package org.skellig.teststep.processing.value.function

import org.skellig.teststep.processing.value.exception.FunctionExecutionException


/**
 * Executes logic for the 'if' function and returns whatever is provided in arg[1] or arg[2], depending on outcome.
 *
 * Supported args:
 * - if(`<condition>`, `<then>`, `<else>`) - evaluates <condition> and returns <then> if true or <else> if false.
 * - if(`<condition>`, `<then>`) - evaluates <condition> and returns <then> if true or null if false.
 *
 */
class IfFunctionExecutor : FunctionValueExecutor {

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any? {
        if (value != null) throw FunctionExecutionException("Function `${getFunctionName()}` cannot be called from another value")

        val condition = (args[0] as Boolean?) ?: error("'condition' is mandatory in 'if' statement")
        val thenValue = args[1] ?: error("'then' is mandatory in 'if' statement")
        val elseValue = if (args.size == 3) args[2] else null

        return if (condition) thenValue else elseValue
    }

    override fun getFunctionName(): String = "if"

}