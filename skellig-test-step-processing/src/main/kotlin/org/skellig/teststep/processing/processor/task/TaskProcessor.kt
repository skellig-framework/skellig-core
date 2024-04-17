package org.skellig.teststep.processing.processor.task

import org.skellig.teststep.reader.value.expression.ValueExpression

/**
 * Represents an interface to process tasks provided from [TaskTestStep][org.skellig.teststep.processing.model.TaskTestStep].
 */
interface TaskProcessor {

    /**
     * Processes a task taken from a raw test step.
     *
     * @param task The task to be processed.
     * @param value The value associated with the task.
     * @param context The [TaskProcessingContext] for task processing.
     */
    fun process(task: ValueExpression?, value: ValueExpression?, context: TaskProcessingContext)

    fun getTaskName(): String

}