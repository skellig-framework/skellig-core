package org.skellig.teststep.processing.model

import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.util.debug
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.reader.value.expression.ValueExpression

/**
 * Represents a property - value pairs to be used by [TestStepProcessor][org.skellig.teststep.processing.processor.TestStepProcessor]
 * for updating [TestScenarioState].
 *
 * @property property The value expression for the property to set in [TestScenarioState].
 * @property value The value expression for the [property] value.
 * @property parameters The parameters to be used in evaluation of [property] and [value].
 * @property valueExpressionContextFactory The factory for creating value expression contexts to be used in evaluation of [property] and [value].
 */
open class ScenarioStateUpdater(
    private val property: ValueExpression,
    private val value: ValueExpression?,
    private val parameters: Map<String, Any?>,
    private val valueExpressionContextFactory: ValueExpressionContextFactory
) {
    private val log = logger <ScenarioStateUpdater>()

    /**
     * Updates the Test Scenario State with evaluated [property] and [value], based on the [result] received from
     * test step processing.
     *
     * @param result The result test step processing, provided inside [TestStepProcessor][org.skellig.teststep.processing.processor.TestStepProcessor].
     * @param state The [TestScenarioState] object to update.
     */
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
