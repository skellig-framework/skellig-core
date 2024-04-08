package org.skellig.teststep.processing.processor.task

import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.MapValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression

internal class StateTaskProcessor(
    private val state: TestScenarioState,
    private val valueConvertDelegate: (ValueExpression?, Map<String, Any?>) -> Any?,
) : TaskProcessor {

    override fun process(task: ValueExpression?, value: ValueExpression?, context: TaskProcessingContext) {
        (task as? AlphanumericValueExpression)?.let {
            when (value) {
                is MapValueExpression -> {
                    value.value.forEach { item ->
                        val key = valueConvertDelegate(item.key, context.parameters)?.toString()
                            ?: error("Cannot set value to the null key in the Test Scenario State")

                        state.set(key, valueConvertDelegate(item.value, context.parameters))
                    }
                }

                else -> error("Invalid property type of the function 'state'. Expected key-value pairs, found ${value?.javaClass}")
            }
        }
    }

    override fun getTaskName(): String = "state"

}