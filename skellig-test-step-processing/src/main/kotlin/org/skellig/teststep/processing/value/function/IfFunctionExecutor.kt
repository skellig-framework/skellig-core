package org.skellig.teststep.processing.value.function

import org.skellig.teststep.processing.value.exception.FunctionExecutionException


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