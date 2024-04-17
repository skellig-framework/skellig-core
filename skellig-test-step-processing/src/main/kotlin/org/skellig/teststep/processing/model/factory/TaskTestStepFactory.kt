package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.TaskTestStep
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.reader.value.expression.ValueExpression

/**
 * This class is responsible for constructing instances of [TaskTestStep].
 *
 * @property testStepRegistry the [TestStepRegistry] used to retrieve test step information
 * @property valueExpressionContextFactory the [ValueExpressionContextFactory] for creating value expression contexts for evaluation of [ValueExpression]
 * @property defaultTestDataConverter the default test data converter to be used (nullable)
 */
internal class TaskTestStepFactory(
    testStepRegistry: TestStepRegistry,
    valueExpressionContextFactory: ValueExpressionContextFactory,
    defaultTestDataConverter: String? = null,
) : BaseDefaultTestStepFactory<TaskTestStep>(testStepRegistry, valueExpressionContextFactory, defaultTestDataConverter) {

    companion object {
        private val TASK = fromProperty("task")
    }

    override fun createTestStepBuilder(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): DefaultTestStep.Builder<TaskTestStep> {
        return TaskTestStep.Builder()
            .withTask(rawTestStep[TASK])
            .withParameters(parameters.toMutableMap())
    }

    override fun isConstructableFrom(rawTestStep: Map<ValueExpression, ValueExpression?>): Boolean {
        return rawTestStep.containsKey(TASK)
    }
}
