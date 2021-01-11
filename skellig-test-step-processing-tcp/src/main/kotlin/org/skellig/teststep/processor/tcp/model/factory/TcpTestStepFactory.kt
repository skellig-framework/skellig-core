package org.skellig.teststep.processor.tcp.model.factory

import org.skellig.teststep.processing.converter.TestDataConverter
import org.skellig.teststep.processing.converter.TestStepValueConverter
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.factory.BaseTestStepFactory
import org.skellig.teststep.processor.tcp.model.TcpTestStep
import java.util.*

class TcpTestStepFactory(keywordsProperties: Properties?,
                         testStepValueConverter: TestStepValueConverter?,
                         testDataConverter: TestDataConverter?)
    : BaseTestStepFactory<TcpTestStep>(keywordsProperties, testStepValueConverter, testDataConverter) {

    companion object {
        private const val PROTOCOL_KEY_KEYWORD = "test.step.keyword.protocol"
        private const val SEND_TO_KEYWORD = "test.step.keyword.sendTo"
        private const val RECEIVE_FROM_KEYWORD = "test.step.keyword.receiveFrom"
        private const val RESPOND_TO_KEYWORD = "test.step.keyword.respondTo"
        private const val BUFFER_SIZE_KEYWORD = "test.step.keyword.bufferSize"
        private const val TCP = "tcp"
    }

    override fun createTestStepBuilder(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): DefaultTestStep.Builder<TcpTestStep> {
        val builder = TcpTestStep.Builder()
                .withSendTo(convertValue(rawTestStep[getKeywordName(SEND_TO_KEYWORD, "sendTo")], parameters))
                .withReceiveFrom(convertValue(rawTestStep[getKeywordName(RECEIVE_FROM_KEYWORD, "readFrom")], parameters))
                .withRespondTo(convertValue(rawTestStep[getKeywordName(RESPOND_TO_KEYWORD, "respondTo")], parameters))
        val readBufferSize = rawTestStep[getKeywordName(BUFFER_SIZE_KEYWORD, "bufferSize")]
        readBufferSize?.let {
            convertValue<Int>(readBufferSize, parameters)?.let { builder.withReadBufferSize(it) }
        }
        return builder
    }

    override fun isConstructableFrom(rawTestStep: Map<String, Any?>): Boolean {
        return rawTestStep.getOrDefault(getKeywordName(PROTOCOL_KEY_KEYWORD, "protocol"), "") == TCP
    }
}