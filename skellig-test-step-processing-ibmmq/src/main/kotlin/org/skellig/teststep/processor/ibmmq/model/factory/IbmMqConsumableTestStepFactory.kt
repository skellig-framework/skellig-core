package org.skellig.teststep.processor.ibmmq.model.factory

import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.processor.ibmmq.model.IbmMqConsumableTestStep
import org.skellig.teststep.reader.value.expression.ValueExpression

class IbmMqConsumableTestStepFactory(
    testStepRegistry: TestStepRegistry,
    valueExpressionContextFactory: ValueExpressionContextFactory,
    defaultTestDataConverter: String? = null
) : BaseIbmMqTestStepFactory<IbmMqConsumableTestStep>(testStepRegistry, valueExpressionContextFactory, defaultTestDataConverter) {

    override fun createTestStepBuilder(
        rawTestStep: Map<ValueExpression, ValueExpression?>,
        parameters: Map<String, Any?>
    ): IbmMqConsumableTestStep.Builder {
        return IbmMqConsumableTestStep.Builder()
            .consumeFrom(getConsumeFromChannels(rawTestStep, parameters))
            .respondTo(getRespondToChannels(rawTestStep, parameters))
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
        rawTestStep.containsKey(getConsumeFromKeyword()) && hasIbmMqRequiredData(rawTestStep)
}