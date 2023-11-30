package org.skellig.teststep.processing.value.function

import org.skellig.task.TaskUtils.Companion.runTask
import org.skellig.teststep.processing.exception.TestDataConversionException
import org.skellig.teststep.processing.state.TestScenarioState
import java.math.BigDecimal
import org.slf4j.LoggerFactory

class GetFromStateFunctionExecutor(val testScenarioState: TestScenarioState) : FunctionValueExecutor {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(GetFromStateFunctionExecutor::class.java)
    }

    override fun execute(name: String, args: Array<Any?>): Any? {
        return when (args.size) {
            1 -> {
                val key = args[0]?.toString() ?: "null"
                testScenarioState.get(key) ?: throw TestDataConversionException("No data found in Test Scenario State with key `$key`")
            }
            3 -> {
                val key = args[0]?.toString() ?: "null"
                val attempts = (args[1] as BigDecimal?) ?: 0
                val delay = (args[2] as BigDecimal?) ?: 0
                LOGGER.info("Trying to get value from the state by key '$key' with $attempts max attempts and $delay ms delay...")
                runTask(
                    { testScenarioState.get(key) },
                    delay.toInt(), attempts.toInt(),
                    { it != null }
                )
            }
            else -> throw TestDataConversionException("Function `get` can only accept 1 or 3 arguments. Found ${args.size}")
        }
    }

    override fun getFunctionName(): String = "get"
}