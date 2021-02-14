package org.skellig.teststep.processor.rmq.model.factory

import org.skellig.teststep.processing.converter.TestDataConverter
import org.skellig.teststep.processing.converter.TestStepValueConverter
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.factory.BaseDefaultTestStepFactory
import org.skellig.teststep.processor.rmq.model.RmqTestStep
import java.util.*

class RmqTestStepFactory(keywordsProperties: Properties?,
                         testStepValueConverter: TestStepValueConverter?,
                         testDataConverter: TestDataConverter?)
    : BaseDefaultTestStepFactory<RmqTestStep>(keywordsProperties, testStepValueConverter, testDataConverter) {

    companion object {
        private const val PROTOCOL_KEY_KEYWORD = "test.step.keyword.protocol"
        private const val ROUTING_KEY_KEYWORD = "test.step.keyword.routingKey"
        private const val SEND_TO_KEYWORD = "test.step.keyword.sendTo"
        private const val RECEIVE_FROM_KEYWORD = "test.step.keyword.receiveFrom"
        private const val RESPOND_TO_KEYWORD = "test.step.keyword.respondTo"
        private const val RMQ_PROPERTIES_KEYWORD = "test.step.keyword.rmq.properties"
        private const val RMQ = "rmq"
    }

    override fun createTestStepBuilder(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): DefaultTestStep.Builder<RmqTestStep> {
        return RmqTestStep.Builder()
                .sendTo(getSendToChannels(rawTestStep, parameters))
                .receiveFrom(getReceiveFromChannels(rawTestStep, parameters))
                .respondTo(getRespondToChannels(rawTestStep, parameters))
                .routingKey(convertValue(getRoutingKey(rawTestStep), parameters))
                .properties(convertValue(getProperties(rawTestStep), parameters))
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

    private fun getRoutingKey(rawTestStep: Map<String, Any?>): Any? =
            rawTestStep[getKeywordName(ROUTING_KEY_KEYWORD, "routingKey")]

    private fun getProperties(rawTestStep: Map<String, Any?>): Any? =
            rawTestStep[getKeywordName(RMQ_PROPERTIES_KEYWORD, "properties")]

    override fun isConstructableFrom(rawTestStep: Map<String, Any?>): Boolean =
            getRoutingKey(rawTestStep) != null || rawTestStep.getOrDefault(getKeywordName(PROTOCOL_KEY_KEYWORD, "protocol"), "") == RMQ

}