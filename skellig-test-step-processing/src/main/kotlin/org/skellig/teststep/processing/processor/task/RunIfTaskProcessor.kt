package org.skellig.teststep.processing.processor.task

import org.skellig.teststep.reader.value.expression.FunctionCallExpression
import org.skellig.teststep.reader.value.expression.MapValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression

internal class RunIfTaskProcessor(
    private val taskProcessor: TaskProcessor,
    private val valueConvertDelegate: (ValueExpression?, Map<String, Any?>) -> Any?,
) : TaskProcessor {

    override fun process(task: ValueExpression?, value: ValueExpression?, context: TaskProcessingContext) {
        (task as? FunctionCallExpression)?.let {
            when (value) {
                is MapValueExpression -> {
                    if (task.args.isNotEmpty()) {
                        if ((valueConvertDelegate(task.args[0], context.parameters) as? Boolean) == true) {
                            taskProcessor.process(null, value, context)
                        }
                    }
                }

                else -> error("Invalid property type of the function '${getTaskName()}'. Expected key-value pairs, found ${value?.javaClass}")
            }
        }
    }

    override fun getTaskName(): String = "runIf"

}