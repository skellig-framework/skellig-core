package org.skellig.teststep.processing.model

import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.util.debug
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.reader.value.expression.ValueExpression

open class ScenarioStateUpdater(
    private val property: ValueExpression,
    private val value: ValueExpression?,
    private val parameters: Map<String, Any?>,
    private val valueExpressionContextFactory: ValueExpressionContextFactory
) {
    private val log = logger <ScenarioStateUpdater>()

    open fun update(result: Any?, state: TestScenarioState) {
        val contextForProperty = valueExpressionContextFactory.create(parameters)
        val contextForValue = valueExpressionContextFactory.create(result, parameters)

        val propertyEvaluated = property.evaluate(contextForProperty)
        val valueEvaluated = value?.evaluate(contextForValue)

        val key = propertyEvaluated?.toString()

        log.debug {"Update Test Scenario State: $key = $valueEvaluated"}
        state.set(key, valueEvaluated)
    }

    override fun toString(): String {
        return "$property = $value"
    }
}
