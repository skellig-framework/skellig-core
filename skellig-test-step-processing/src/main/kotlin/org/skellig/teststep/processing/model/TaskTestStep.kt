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