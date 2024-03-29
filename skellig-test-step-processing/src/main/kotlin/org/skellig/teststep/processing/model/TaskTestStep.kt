package org.skellig.teststep.processing.model

import org.skellig.teststep.reader.value.expression.ValueExpression


class TaskTestStep(
    id: String?,
    name: String?,
    execution: TestStepExecutionType?,
    timeout: Int,
    delay: Int,
    attempts: Int,
    values: Map<String, Any?>?,
    testData: Any?,
    validationDetails: ValidationNode?,
    scenarioStateUpdaters: List<ScenarioStateUpdater>?,
    val task: ValueExpression?,
    val parameters: MutableMap<String, Any?>,
) : DefaultTestStep(id, name!!, execution, timeout, delay, attempts, values, testData, validationDetails, scenarioStateUpdaters) {


    override fun toString(): String {
        return super.toString() + "task = $task\n"
    }

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
            return TaskTestStep(
                id, name, execution, timeout, delay, attempts, values, testData,
                validationDetails, scenarioStateUpdaters, task,
                parameters ?: error("Parameters are mandatory for TaskTestStep")
            )
        }
    }
}