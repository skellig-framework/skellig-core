package org.skellig.teststep.processing.value.function

import org.skellig.teststep.processing.exception.TestDataConversionException
import org.skellig.teststep.processing.state.TestScenarioState

class GetFromStateFunctionExecutor(val testScenarioState: TestScenarioState) : FunctionValueExecutor {

    override fun execute(name: String, args: Array<Any?>): Any {
        if (args.size == 1) {
            val key = args[0]?.toString() ?: "null"
            return testScenarioState.get(key) ?: throw TestDataConversionException("No data found in Test Scenario State with key `$key`")
        } else {
            throw TestDataConversionException("Function `get` can only accept 1 String argument. Found ${args.size}")
        }
    }

    override fun getFunctionName(): String = "get"
}