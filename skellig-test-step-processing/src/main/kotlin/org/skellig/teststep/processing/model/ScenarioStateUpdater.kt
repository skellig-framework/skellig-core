package org.skellig.teststep.processing.model

import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.reader.value.expression.ValueExpression

open class ScenarioStateUpdater(
    private val property: ValueExpression,
    private val value: ValueExpression?,
    private val parameters: Map<String, Any?>,
    private val valueExpressionContextFactory: ValueExpressionContextFactory
) {

    open fun update(result: Any?, state: TestScenarioState) {
        val contextForProperty = valueExpressionContextFactory.create(parameters)
        val contextForValue = valueExpressionContextFactory.create(result, parameters)

        val actualEvaluated = property.evaluate(contextForProperty)
        val expectedEvaluated = value?.evaluate(contextForValue)

        state.set(actualEvaluated?.toString(), expectedEvaluated)
    }

    fun toString(indent: Int): String {
        return "${"\t".repeat(indent)}$property = $value"
    }
}
