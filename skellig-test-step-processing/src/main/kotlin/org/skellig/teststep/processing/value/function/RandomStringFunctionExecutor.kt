package org.skellig.teststep.processing.value.function

import org.apache.commons.lang3.RandomStringUtils
import org.skellig.teststep.processing.value.function.exception.FunctionValueExecutionException
import java.lang.NumberFormatException


class RandomStringFunctionExecutor : FunctionValueExecutor {

    override fun execute(name: String, args: Array<Any?>): Any {
        if (args.size == 1) {
            try {
                val size = args[0].toString().toInt()
                return RandomStringUtils.random(size, true, true)
            }catch (ex: NumberFormatException) {
                throw FunctionValueExecutionException("Failed to generate random string with parameter '${args[0]}'", ex)
            }
        } else {
            throw FunctionValueExecutionException("Function `randString` can only accept 1 argument = size of random string. Found ${args.size}")
        }
    }

    override fun getFunctionName(): String = "randString"
}