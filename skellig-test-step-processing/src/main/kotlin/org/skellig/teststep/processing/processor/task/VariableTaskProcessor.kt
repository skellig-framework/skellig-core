package org.skellig.teststep.processing.processor.task

import org.skellig.teststep.reader.value.expression.ValueExpression

internal class VariableTaskProcessor(
    private val valueConvertDelegate: (ValueExpression?, Map<String, Any?>) -> Any?,
) : TaskProcessor {

    override fun process(task: ValueExpression?, value: ValueExpression?, context: TaskProcessingContext) {
        val key = valueConvertDelegate(task, context.parameters)?.toString()
            ?: error("Cannot assign value to the null key")
        context.parameters[key] = valueConvertDelegate(value, context.parameters)
    }

    override fun getTaskName(): String = ""

}