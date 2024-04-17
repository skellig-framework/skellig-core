package org.skellig.teststep.processing.value.function.collection

import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import org.skellig.teststep.processing.value.function.FunctionValueExecutor

/**
 * This function adds elements to a [Collection] or [Map], or throws [FunctionExecutionException] otherwise.
 * The result is a new [Collection] or [Map] which has the values from arguments.
 *
 * Supported args:
 * - add(`<value>`) - where `<value>` can be any type of value
 */
class AddFunctionExecutor : FunctionValueExecutor {

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any {
        return if (args.isNotEmpty()) {
            when (value) {
                is Collection<*> -> {
                    val mutableCollection = when (value) {
                        is Set<*> -> value.toMutableSet()
                        else -> value.toMutableList()
                    }
                    args.forEach { mutableCollection.add(it) }
                    mutableCollection
                }

                is Map<*, *> -> {
                    val mutableMap = value.toMutableMap()
                    args.forEach {
                        if (it is Map<*, *>) mutableMap.putAll(it)
                        else throw FunctionExecutionException("The function '${getFunctionName()}' of Map value can only accept Map as an arguments. Found: ${it?.javaClass}")
                    }
                    mutableMap
                }

                else -> throw FunctionExecutionException("The function '${getFunctionName()}' can only accept Collection or Map as value. Found: ${value?.javaClass}")
            }
        } else throw FunctionExecutionException("Failed to execute function '${getFunctionName()}' as it must have at least 1 argument")
    }

    override fun getFunctionName(): String = "add"

}