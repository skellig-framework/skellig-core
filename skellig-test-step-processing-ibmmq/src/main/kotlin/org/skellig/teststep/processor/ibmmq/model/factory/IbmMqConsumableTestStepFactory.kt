package org.skellig.teststep.processor.ibmmq.model.factory

import org.skellig.teststep.processing.converter.TestStepValueConverter
import org.skellig.teststep.processor.ibmmq.model.IbmMqConsumableTestStep
import java.util.*

class IbmMqConsumableTestStepFactory(keywordsProperties: Properties?,
                                     testStepValueConverter: TestStepValueConverter?)
    : BaseIbmMqTestStepFactory<IbmMqConsumableTestStep>(keywordsProperties, testStepValueConverter) {

    override fun createTestStepBuilder(rawTestStep: Map<String, Any?>,
                                       parameters: Map<String, Any?>): IbmMqConsumableTestStep.Builder {
        return IbmMqConsumableTestStep.Builder()
            .consumeFrom(getConsumeFromChannels(rawTestStep, parameters))
            .respondTo(getRespondToChannels(rawTestStep, parameters))
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
        rawTestStep.containsKey(getConsumeFromKeyword()) && hasIbmMqRequiredData(rawTestStep)
}