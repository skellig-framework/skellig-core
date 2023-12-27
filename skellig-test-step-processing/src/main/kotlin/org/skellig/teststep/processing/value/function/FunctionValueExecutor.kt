package org.skellig.teststep.processing.value.function

interface FunctionValueExecutor {

    fun execute(name: String, value: Any?, args: Array<Any?>): Any?

    fun getFunctionName(): String
}