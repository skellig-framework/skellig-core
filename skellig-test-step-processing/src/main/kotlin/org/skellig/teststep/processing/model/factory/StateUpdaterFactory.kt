package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.model.ScenarioStateUpdater
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.MapValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression

/**
 * The StateUpdaterFactory class is responsible for creating [ScenarioStateUpdater] instances from a raw test step.
 * It uses a [ValueExpressionContextFactory] to create the necessary [ValueExpressionContext] objects for evaluation.
 *
 * @property valueExpressionContextFactory The [ValueExpressionContextFactory] to use for creating [ValueExpressionContext] objects.
 */
internal class StateUpdaterFactory(private val valueExpressionContextFactory: ValueExpressionContextFactory) {

    companion object {
        private val STATE = AlphanumericValueExpression("state")
    }

    fun create(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): List<ScenarioStateUpdater>? {
        return if (rawTestStep.containsKey(STATE))
            (rawTestStep[STATE] as? MapValueExpression)?.value?.map {
                ScenarioStateUpdater(it.key, it.value, parameters, valueExpressionContextFactory)
            }?.toList()
        else null
    }
}