package org.skellig.teststep.processing.processor.task

import org.skellig.teststep.reader.value.expression.FunctionCallExpression
import org.skellig.teststep.reader.value.expression.MapValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class RunIfTaskProcessor(
    private val taskProcessor: TaskProcessor,
    private val valueConvertDelegate: (ValueExpression?, Map<String, Any?>) -> Any?,
) : TaskProcessor {

    private val log: Logger = LoggerFactory.getLogger(RunIfTaskProcessor::class.java)

    override fun process(task: ValueExpression?, value: ValueExpression?, context: TaskProcessingContext) {
        (task as? FunctionCallExpression)?.let {
            when (value) {
                is MapValueExpression -> {
                    if (task.args.isNotEmpty()) {
                        if ((valueConvertDelegate(task.args[0], context.parameters) as? Boolean) == true) {
                            taskProcessor.process(null, value, context)
                        } else log.debug("The condition '${task.args[0]}' of the task '${getTaskName()}' has evaluated to false")
                    } else error("No arguments found for '${getTaskName()}'. Expected 1, found 0")
                }

                else -> error("Invalid property type of the function '${getTaskName()}'. Expected key-value pairs, found ${value?.javaClass}")
            }
        }
    }

    override fun getTaskName(): String = "runIf"

}