package org.skellig.teststep.processor.ibmmq.model.factory

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.factory.TestStepFactoryValueConverter
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processor.ibmmq.model.IbmMqTestStep
import java.util.*

open class IbmMqTestStepFactory(testStepRegistry: TestStepRegistry,
                                keywordsProperties: Properties?,
                                testStepFactoryValueConverter: TestStepFactoryValueConverter)
    : BaseIbmMqTestStepFactory<IbmMqTestStep>(testStepRegistry, keywordsProperties, testStepFactoryValueConverter) {

    companion object {
        private const val SEND_TO_KEYWORD = "test.step.keyword.sendTo"
        private const val RECEIVE_FROM_KEYWORD = "test.step.keyword.receiveFrom"
        private const val DEFAULT_DELAY = 250
        private const val DEFAULT_ATTEMPTS = 20
    }

    override fun createTestStepBuilder(rawTestStep: Map<Any, Any?>, parameters: Map<String, Any?>): DefaultTestStep.Builder<IbmMqTestStep> {
        return IbmMqTestStep.Builder()
                .sendTo(getSendToChannels(rawTestStep, parameters))
                .readFrom(getReceiveFromChannels(rawTestStep, parameters))
                .respondTo(getRespondToChannels(rawTestStep, parameters))
    }

    private fun getRespondToChannels(rawTestStep: Map<Any, Any?>, parameters: Map<String, Any?>): Set<String>? =
            toSet(convertValue<Any>(rawTestStep[getKeywordName(RESPOND_TO_KEYWORD, "respondTo")], parameters))

    private fun getSendToChannels(rawTestStep: Map<Any, Any?>, parameters: Map<String, Any?>): Set<String>? =
            toSet(convertValue<Any>(rawTestStep[getKeywordName(SEND_TO_KEYWORD, "sendTo")], parameters))

    private fun getReceiveFromChannels(rawTestStep: Map<Any, Any?>, parameters: Map<String, Any?>): Set<String>? =
            toSet(convertValue<Any>(rawTestStep[getKeywordName(RECEIVE_FROM_KEYWORD, "readFrom")], parameters))

    private fun toSet(channel: Any?): Set<String>? {
        return when (channel) {
            is Collection<*> -> channel.map { it.toString() }.toSet()
            else -> channel?.let { setOf(channel.toString()) }
        }
    }

    override fun isConstructableFrom(rawTestStep: Map<Any, Any?>): Boolean =
        !rawTestStep.containsKey(getConsumeFromKeyword()) && hasIbmMqRequiredData(rawTestStep)

    override fun getDelay(rawTestStep: Map<Any, Any?>, parameters: Map<String, Any?>): Int {
        val delay = super.getDelay(rawTestStep, parameters)
        return if (delay == 0) DEFAULT_DELAY else delay
    }

    override fun getAttempts(rawTestStep: Map<Any, Any?>, parameters: Map<String, Any?>): Int {
        val attempts = super.getAttempts(rawTestStep, parameters)
        return if (attempts == 0) DEFAULT_ATTEMPTS else attempts
    }

}