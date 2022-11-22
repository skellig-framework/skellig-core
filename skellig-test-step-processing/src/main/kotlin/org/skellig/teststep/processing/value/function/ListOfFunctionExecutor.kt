package org.skellig.teststep.processing.value.function

internal class ListOfFunctionExecutor : FunctionValueExecutor {

    override fun execute(name: String, args: Array<Any?>): Any = args.toList()

    override fun getFunctionName(): String = "listOf"
}