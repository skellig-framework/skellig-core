package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.model.TaskTestStep
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.reader.value.expression.ValueExpression

internal class TaskTestStepFactory(
    private val testStepRegistry: TestStepRegistry,
    private val testStepFactory: TestStepFactory<TestStep>,
    valueExpressionContextFactory: ValueExpressionContextFactory
) : BaseTestStepFactory<TaskTestStep>(valueExpressionContextFactory) {

    companion object {
        private val TASK = fromProperty("task")
    }

    override fun create(testStepName: String, rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, String?>): TaskTestStep {
        val additionalParameters = extractParametersFromTestStepName(testStepName, rawTestStep)
        additionalParameters?.putAll(parameters)

        return TaskTestStep(testStepName, rawTestStep[TASK], additionalParameters) { v, p -> convertValue(v, p) }
    }

    override fun isConstructableFrom(rawTestStep: Map<ValueExpression, ValueExpression?>): Boolean {
        return rawTestStep.containsKey(TASK)
    }
}
