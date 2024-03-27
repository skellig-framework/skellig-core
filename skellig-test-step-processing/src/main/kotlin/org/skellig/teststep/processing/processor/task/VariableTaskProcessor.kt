package org.skellig.teststep.processing.processor.task

import org.skellig.teststep.reader.value.expression.ValueExpression

internal class VariableTaskProcessor(
    private val valueConvertDelegate: (ValueExpression?, Map<String, Any?>) -> Any?,
) : TaskProcessor {

    override fun process(task: ValueExpression?, value: ValueExpression?, parameters: MutableMap<String, Any?>) {
        val key = valueConvertDelegate(task, parameters)?.toString()
            ?: error("Cannot assign value to the null key")
        parameters[key] = valueConvertDelegate(value, parameters)
    }

    override fun getTaskName(): String = ""

}