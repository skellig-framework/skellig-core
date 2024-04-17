package org.skellig.teststep.processor.rmq.model.factory

import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.processor.rmq.model.BaseRmqTestStep
import org.skellig.teststep.processor.rmq.model.RmqConsumableTestStep
import org.skellig.teststep.reader.value.expression.ValueExpression

/**
 * Factory class for creating [RmqConsumableTestStep] instances.
 *
 * @property testStepRegistry The test step registry for retrieving test steps.
 * @property valueExpressionContextFactory The value expression context factory used to create the value expression contexts
 * for evaluation of [ValueExpression].
 * @property defaultTestDataConverter The name of default converter (function) for test data (optional).
 */
class RmqConsumableTestStepFactory(
    testStepRegistry: TestStepRegistry,
    valueExpressionContextFactory: ValueExpressionContextFactory,
    defaultTestDataConverter: String? = null
) : BaseRmqTestStepFactory<RmqConsumableTestStep>(testStepRegistry, valueExpressionContextFactory, defaultTestDataConverter) {

    override fun createTestStepBuilder(
        rawTestStep: Map<ValueExpression, ValueExpression?>,
        parameters: Map<String, Any?>
    ): BaseRmqTestStep.Builder<RmqConsumableTestStep> {
        return RmqConsumableTestStep.Builder()
            .consumeFrom(getConsumeFromChannels(rawTestStep, parameters))
            .respondTo(getRespondToChannels(rawTestStep, parameters))
            .routingKey(convertValue(getRoutingKey(rawTestStep), parameters))
            .properties(convertValue(getProperties(rawTestStep), parameters))
    }

    private fun getRespondToChannels(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): List<String>? =
        toList(convertValue<Any>(rawTestStep[RESPOND_TO_KEYWORD], parameters))

    private fun getConsumeFromChannels(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): List<String>? =
        toList(convertValue<Any>(rawTestStep[getConsumeFromKeyword()], parameters))

    private fun toList(channel: Any?): List<String>? {
        return when (channel) {
            is Collection<*> -> channel.map { it.toString() }.toList()
            else -> channel?.let { listOf(channel.toString()) }
        }
    }

    override fun isConstructableFrom(rawTestStep: Map<ValueExpression, ValueExpression?>): Boolean =
        rawTestStep.containsKey(getConsumeFromKeyword()) && hasRmqRequiredData(rawTestStep)
}