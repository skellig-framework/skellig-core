package org.skellig.teststep.processing.value.function

import org.skellig.task.TaskUtils.Companion.runTask
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import org.slf4j.LoggerFactory
import java.math.BigDecimal

/**
 * Function 'get' is responsible for retrieving values from the [TestScenarioState].
 *
 * Supported args:
 * - get(`<key>`) - gets value from [TestScenarioState] by any key name
 * - get(`<key>`, `<default>`) - same as above but if not found, sets the `<default>` value into [TestScenarioState] for the `<key>`
 * and returns it back.
 * - get(`<key>`, `<attempts>`, `<delay>`) - periodically tries to get value from [TestScenarioState] with max `<attempts>` and
 * delaying next attempt on `<delay>` milliseconds.
 *
 * @property testScenarioState the [TestScenarioState] instance to retrieve values from
 */
class GetFromStateFunctionExecutor(val testScenarioState: TestScenarioState) : FunctionValueExecutor {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(GetFromStateFunctionExecutor::class.java)
    }

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any? {
        return when (args.size) {
            1 -> {
                val key = args[0]?.toString() ?: "null"
                testScenarioState.get(key) ?: throw FunctionExecutionException("No data found in Test Scenario State with key `$key`")
            }

            2 -> {
                val key = args[0]?.toString() ?: "null"
                var storedValue = testScenarioState.get(key)
                if (storedValue == null) {
                    storedValue = args[1]
                    testScenarioState.set(key, storedValue)
                }
                storedValue
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

            else -> throw FunctionExecutionException("Function `${getFunctionName()}` can only accept 1, 2 or 3 arguments. Found ${args.size}")
        }
    }

    override fun getFunctionName(): String = "get"
}