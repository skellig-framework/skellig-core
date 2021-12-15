package org.skellig.teststep.processor.rmq.model.factory

import org.skellig.teststep.processing.model.factory.TestStepFactoryValueConverter
import org.skellig.teststep.processor.rmq.model.BaseRmqTestStep
import org.skellig.teststep.processor.rmq.model.RmqConsumableTestStep
import java.util.*

class RmqConsumableTestStepFactory(keywordsProperties: Properties?,
                                   testStepFactoryValueConverter: TestStepFactoryValueConverter)
    : BaseRmqTestStepFactory<RmqConsumableTestStep>(keywordsProperties, testStepFactoryValueConverter) {

    override fun createTestStepBuilder(rawTestStep: Map<String, Any?>,
                                       parameters: Map<String, Any?>): BaseRmqTestStep.Builder<RmqConsumableTestStep> {
        return RmqConsumableTestStep.Builder()
            .consumeFrom(getConsumeFromChannels(rawTestStep, parameters))
            .respondTo(getRespondToChannels(rawTestStep, parameters))
            .routingKey(convertValue(getRoutingKey(rawTestStep), parameters))
            .properties(convertValue(getProperties(rawTestStep), parameters))
    }

    private fun getRespondToChannels(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): List<String>? =
        toList(convertValue<Any>(rawTestStep[getKeywordName(RESPOND_TO_KEYWORD, "respondTo")], parameters))

    private fun getConsumeFromChannels(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): List<String>? =
        toList(convertValue<Any>(rawTestStep[getConsumeFromKeyword()], parameters))

    private fun toList(channel: Any?): List<String>? {
        return when (channel) {
            is Collection<*> -> channel.map { it.toString() }.toList()
            else -> channel?.let { listOf(channel.toString()) }
        }
    }

    override fun isConstructableFrom(rawTestStep: Map<String, Any?>): Boolean =
        rawTestStep.containsKey(getConsumeFromKeyword()) && hasRmqRequiredData(rawTestStep)
}