package org.skellig.teststep.processor.ibmmq.model.factory

import org.skellig.teststep.processing.converter.TestStepValueConverter
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.factory.BaseDefaultTestStepFactory
import org.skellig.teststep.processor.ibmmq.model.IbmMqTestStep
import java.util.*

open class IbmMqTestStepFactory(keywordsProperties: Properties?,
                                testStepValueConverter: TestStepValueConverter?)
    : BaseDefaultTestStepFactory<IbmMqTestStep>(keywordsProperties, testStepValueConverter) {

    companion object {
        private const val PROTOCOL_KEY_KEYWORD = "test.step.keyword.protocol"
        private const val SEND_TO_KEYWORD = "test.step.keyword.sendTo"
        private const val RECEIVE_FROM_KEYWORD = "test.step.keyword.receiveFrom"
        private const val RESPOND_TO_KEYWORD = "test.step.keyword.respondTo"
        private const val IBMMQ = "ibmmq"
        private const val DEFAULT_DELAY = 250
        private const val DEFAULT_ATTEMPTS = 20
    }

    override fun createTestStepBuilder(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): DefaultTestStep.Builder<IbmMqTestStep> {
        return IbmMqTestStep.Builder()
                .sendTo(getSendToChannels(rawTestStep, parameters))
                .readFrom(getReceiveFromChannels(rawTestStep, parameters))
                .respondTo(getRespondToChannels(rawTestStep, parameters))
    }

    private fun getRespondToChannels(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): Set<String>? =
            toSet(convertValue<Any>(rawTestStep[getKeywordName(RESPOND_TO_KEYWORD, "respondTo")], parameters))

    private fun getSendToChannels(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): Set<String>? =
            toSet(convertValue<Any>(rawTestStep[getKeywordName(SEND_TO_KEYWORD, "sendTo")], parameters))

    private fun getReceiveFromChannels(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): Set<String>? =
            toSet(convertValue<Any>(rawTestStep[getKeywordName(RECEIVE_FROM_KEYWORD, "readFrom")], parameters))

    private fun toSet(channel: Any?): Set<String>? {
        return when (channel) {
            is Collection<*> -> channel.map { it.toString() }.toSet()
            else -> channel?.let { setOf(channel.toString()) }
        }
    }

    override fun isConstructableFrom(rawTestStep: Map<String, Any?>): Boolean {
        return rawTestStep.getOrDefault(getKeywordName(PROTOCOL_KEY_KEYWORD, "protocol"), "") == IBMMQ
    }

    override fun getDelay(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): Int {
        val delay = super.getDelay(rawTestStep, parameters)
        return if (delay == 0) DEFAULT_DELAY else delay
    }

    override fun getAttempts(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): Int {
        val attempts = super.getAttempts(rawTestStep, parameters)
        return if (attempts == 0) DEFAULT_ATTEMPTS else attempts
    }
}