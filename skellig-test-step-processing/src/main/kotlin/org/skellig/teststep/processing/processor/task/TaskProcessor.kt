package org.skellig.teststep.processing.processor.task

import org.skellig.teststep.reader.value.expression.ValueExpression

interface TaskProcessor {

    fun process(task: ValueExpression?, value: ValueExpression?, context: TaskProcessingContext)

    fun getTaskName(): String

}