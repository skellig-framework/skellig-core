package org.skellig.teststep.processing.model

import org.skellig.teststep.reader.value.expression.ValueExpression

/**
 * The `TaskTestStep` class represents a test step that provides a list of [tasks][org.skellig.teststep.processing.processor.task.TaskProcessor]
 * for execution in [TaskTestStepProcessor][org.skellig.teststep.processing.processor.task.TaskTestStepProcessor].
 *
 * @param id The unique identifier for the test step.
 * @param name The name of the test step.
 * @param execution The type of test step execution (`SYNC` or `ASYNC`).
 * @param timeout The maximum duration (in milliseconds) for the task to complete.
 * @param delay The delay (in milliseconds) to wait before executing the task.
 * @param attempts The maximum number of times to attempt executing the task.
 * @param values The map of values required for the task.
 * @param testData The value expression representing the task to be executed.
 * @param validationDetails The validation node for validating the task output.
 * @param scenarioStateUpdaters The list of [ScenarioStateUpdater] to update the test scenario state after executing the task.
 * @param parameters The mutable map of additional parameters specific to the task.
 */
class TaskTestStep(
    id: String?,
    name: String?,
    execution: TestStepExecutionType,
    timeout: Int,
    delay: Int,
    attempts: Int,
    values: Map<String, Any?>?,
    testData: ValueExpression?,
    validationDetails: ValidationNode?,
    scenarioStateUpdaters: List<ScenarioStateUpdater>?,
    val parameters: MutableMap<String, Any?>,
) : DefaultTestStep(id, name!!, execution, timeout, delay, attempts, values, testData, validationDetails, scenarioStateUpdaters) {

    fun getTask(): ValueExpression? = testData as? ValueExpression?

    class Builder : DefaultTestStep.Builder<TaskTestStep>() {

        private var task: ValueExpression? = null
        private var parameters: MutableMap<String, Any?>? = null

        fun withTask(task: ValueExpression?) = apply {
            this.task = task
        }

        fun withParameters(parameters: MutableMap<String, Any?>?) = apply {
            this.parameters = parameters
        }

        override fun build(): TaskTestStep {
            if (task == null) task = testData as? ValueExpression?
            return TaskTestStep(
                id, name, execution, timeout, delay, attempts, values, task,
                validationDetails, scenarioStateUpdaters,
                parameters ?: error("Parameters are mandatory for TaskTestStep")
            )
        }
    }
}