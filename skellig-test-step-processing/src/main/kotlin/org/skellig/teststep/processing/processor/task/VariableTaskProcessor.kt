package org.skellig.teststep.processing.processor.task

import org.skellig.teststep.reader.value.expression.ValueExpression
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class VariableTaskProcessor(
    private val valueConvertDelegate: (ValueExpression?, Map<String, Any?>) -> Any?,
) : TaskProcessor {

    private val log: Logger = LoggerFactory.getLogger(VariableTaskProcessor::class.java)

    override fun process(task: ValueExpression?, value: ValueExpression?, context: TaskProcessingContext) {
        val key = valueConvertDelegate(task, context.parameters)?.toString()
            ?: error("Cannot assign value to the null key")
        val convertedValue = valueConvertDelegate(value, context.parameters)
        context.parameters[key] = convertedValue

        log.info("Assign variable '$key' with value '$convertedValue'")
    }

    override fun getTaskName(): String = ""

}