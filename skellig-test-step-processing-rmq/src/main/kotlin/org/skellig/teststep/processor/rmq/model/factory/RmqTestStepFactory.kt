package org.skellig.teststep.processor.rmq.model.factory

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.processor.rmq.model.RmqTestStep
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression

class RmqTestStepFactory(
    testStepRegistry: TestStepRegistry,
    valueExpressionContextFactory: ValueExpressionContextFactory
) : BaseRmqTestStepFactory<RmqTestStep>(testStepRegistry, valueExpressionContextFactory) {

    companion object {
        private val SEND_TO_KEYWORD = AlphanumericValueExpression("sendTo")
        private val RECEIVE_FROM_KEYWORD = AlphanumericValueExpression("receiveFrom")
        private const val DEFAULT_DELAY = 250
        private const val DEFAULT_ATTEMPTS = 20
    }

    override fun createTestStepBuilder(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): DefaultTestStep.Builder<RmqTestStep> {
        return RmqTestStep.Builder()
            .sendTo(getSendToChannels(rawTestStep, parameters))
            .receiveFrom(getReceiveFromChannels(rawTestStep, parameters))
            .respondTo(getRespondToChannels(rawTestStep, parameters))
            .routingKey(convertValue(getRoutingKey(rawTestStep), parameters))
            .properties(convertValue(getProperties(rawTestStep), parameters))
    }

    private fun getRespondToChannels(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): Set<String>? =
        toSet(convertValue<Any>(rawTestStep[RESPOND_TO_KEYWORD], parameters))

    private fun getSendToChannels(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): Set<String>? =
        toSet(convertValue<Any>(rawTestStep[SEND_TO_KEYWORD], parameters))

    private fun getReceiveFromChannels(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): Set<String>? =
        toSet(convertValue<Any>(rawTestStep[RECEIVE_FROM_KEYWORD], parameters))

    private fun toSet(channel: Any?): Set<String>? {
        return when (channel) {
            is Collection<*> -> channel.map { it.toString() }.toSet()
            else -> channel?.let { setOf(channel.toString()) }
        }
    }

    override fun isConstructableFrom(rawTestStep: Map<ValueExpression, ValueExpression?>): Boolean =
        !rawTestStep.containsKey(getConsumeFromKeyword()) && hasRmqRequiredData(rawTestStep)

    override fun getDelay(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): Int {
        val delay = super.getDelay(rawTestStep, parameters)
        return if (delay == 0) DEFAULT_DELAY else delay
    }

    override fun getAttempts(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): Int {
        val attempts = super.getAttempts(rawTestStep, parameters)
        return if (attempts == 0) DEFAULT_ATTEMPTS else attempts
    }
}