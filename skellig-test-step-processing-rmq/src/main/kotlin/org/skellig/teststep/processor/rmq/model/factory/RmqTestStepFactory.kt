package org.skellig.teststep.processor.rmq.model.factory

import org.skellig.teststep.processing.converter.TestDataConverter
import org.skellig.teststep.processing.converter.TestStepValueConverter
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.factory.BaseTestStepFactory
import org.skellig.teststep.processor.rmq.model.RmqTestStep
import java.util.*

class RmqTestStepFactory(keywordsProperties: Properties?,
                         testStepValueConverter: TestStepValueConverter?,
                         testDataConverter: TestDataConverter?)
    : BaseTestStepFactory<RmqTestStep>(keywordsProperties, testStepValueConverter, testDataConverter) {

    companion object {
        private const val PROTOCOL_KEY_KEYWORD = "test.step.keyword.protocol"
        private const val ROUTING_KEY_KEYWORD = "test.step.keyword.routingKey"
        private const val SEND_TO_KEYWORD = "test.step.keyword.sendTo"
        private const val RECEIVE_FROM_KEYWORD = "test.step.keyword.receiveFrom"
        private const val RESPOND_TO_KEYWORD = "test.step.keyword.respondTo"
        private const val RMQ = "rmq"
    }

    override fun createTestStepBuilder(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): DefaultTestStep.Builder<RmqTestStep> {
        return RmqTestStep.Builder()
                .withSendTo(convertValue(rawTestStep[getKeywordName(SEND_TO_KEYWORD, "sendTo")], parameters))
                .withReceiveFrom(convertValue(rawTestStep[getKeywordName(RECEIVE_FROM_KEYWORD, "readFrom")], parameters))
                .withRespondTo(convertValue(rawTestStep[getKeywordName(RESPOND_TO_KEYWORD, "respondTo")], parameters))
                .withRoutingKey(convertValue(getRoutingKey(rawTestStep), parameters))
    }

    override fun isConstructableFrom(rawTestStep: Map<String, Any?>): Boolean {
        return getRoutingKey(rawTestStep) != null || rawTestStep.getOrDefault(getKeywordName(PROTOCOL_KEY_KEYWORD, "protocol"), "") == RMQ
    }

    private fun getRoutingKey(rawTestStep: Map<String, Any?>): Any? {
        return rawTestStep[getKeywordName(ROUTING_KEY_KEYWORD, "routingKey")]
    }

}