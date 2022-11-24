package org.skellig.teststep.processing.value.function

import java.util.regex.Pattern

internal class ListOfFunctionExecutor : FunctionValueExecutor {

    companion object {
        private val SEPARATOR_REGEX = Pattern.compile(",")
    }

    override fun execute(name: String, args: Array<Any?>): Any {
        return if (args.size == 1 && args[0] is String)
            SEPARATOR_REGEX.split(args[0].toString()).map { it.trim() }.filter { it.trim().isNotEmpty() }.toList()
        else args.toList()
    }

    override fun getFunctionName(): String = "listOf"
}