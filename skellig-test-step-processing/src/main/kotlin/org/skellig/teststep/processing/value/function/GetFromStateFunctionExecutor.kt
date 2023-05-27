package org.skellig.teststep.processing.value.function

import org.skellig.teststep.processing.value.function.exception.FunctionValueExecutionException
import org.skellig.teststep.processing.state.TestScenarioState

class GetFromStateFunctionExecutor(val testScenarioState: TestScenarioState) : FunctionValueExecutor {

    override fun execute(name: String, args: Array<Any?>): Any {
        if (args.size == 1) {
            val key = args[0]?.toString() ?: "null"
            return testScenarioState.get(key) ?: throw FunctionValueExecutionException("No data found in Test Scenario State with key `$key`")
        } else {
            throw FunctionValueExecutionException("Function `get` can only accept 1 String argument. Found ${args.size}")
        }
    }

    override fun getFunctionName(): String = "get"
}