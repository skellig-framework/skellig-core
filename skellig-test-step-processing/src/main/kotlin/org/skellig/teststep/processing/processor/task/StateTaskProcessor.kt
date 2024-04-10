package org.skellig.teststep.processing.processor.task

import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.MapValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class StateTaskProcessor(
    private val state: TestScenarioState,
    private val valueConvertDelegate: (ValueExpression?, Map<String, Any?>) -> Any?,
) : TaskProcessor {

    private val log: Logger = LoggerFactory.getLogger(RunTestTaskProcessor::class.java)

    override fun process(task: ValueExpression?, value: ValueExpression?, context: TaskProcessingContext) {
        (task as? AlphanumericValueExpression)?.let {
            when (value) {
                is MapValueExpression -> {
                    value.value.forEach { item ->
                        val key = valueConvertDelegate(item.key, context.parameters)?.toString()
                            ?: error("Cannot set value to the null key in the Test Scenario State")

                        val convertedValue = valueConvertDelegate(item.value, context.parameters)
                        state.set(key, convertedValue)

                        log.info("Assign state key '$key' with value '$convertedValue'")
                    }
                }

                else -> error("Invalid property type of the function 'state'. Expected key-value pairs, found ${value?.javaClass}")
            }
        }
    }

    override fun getTaskName(): String = "state"

}