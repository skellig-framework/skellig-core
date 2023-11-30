package org.skellig.teststep.processing.value.function

class IfFunctionExecutor : FunctionValueExecutor {

    override fun execute(name: String, args: Array<Any?>): Any? {

        val condition = (args[0] as Boolean?) ?: error("'condition' is mandatory in 'if' statement")
        val thenValue = args[1] ?: error("'then' is mandatory in 'if' statement")
        val elseValue = if (args.size == 3) args[2] else null

        return if (condition) thenValue else elseValue
    }

    override fun getFunctionName(): String = "if"

}