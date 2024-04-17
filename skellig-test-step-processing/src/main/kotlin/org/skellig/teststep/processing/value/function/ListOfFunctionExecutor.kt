package org.skellig.teststep.processing.value.function

import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import java.util.regex.Pattern

/**
 * Executes the `listOf` function that creates a [List] from the provided arguments.
 *
 * Supported args:
 * - listOf(`<items>`) - where `<items>` are comma-separated values to be added to [List],
 * or a [String] with comma-separated [String] values.
 */
internal class ListOfFunctionExecutor : FunctionValueExecutor {

    companion object {
        private val SEPARATOR_REGEX = Pattern.compile(",")
    }

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any {
        if (value != null) throw FunctionExecutionException("Function `${getFunctionName()}` cannot be called from another value")

        return if (args.size == 1 && args[0] is String)
            SEPARATOR_REGEX.split(args[0].toString()).map { it.trim() }.filter { it.trim().isNotEmpty() }.toList()
        else args.toList()
    }

    override fun getFunctionName(): String = "listOf"
}