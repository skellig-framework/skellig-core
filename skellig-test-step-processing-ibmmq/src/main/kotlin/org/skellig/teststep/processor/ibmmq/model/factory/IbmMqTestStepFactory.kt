package org.skellig.teststep.processor.ibmmq.model.factory

import org.skellig.teststep.processing.converter.TestStepValueConverter
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.factory.BaseDefaultTestStepFactory
import org.skellig.teststep.processor.ibmmq.model.IbmMqTestStep
import java.util.*

open class IbmMqTestStepFactory(keywordsProperties: Properties?,
                                testStepValueConverter: TestStepValueConverter?)
    : BaseDefaultTestStepFactory<IbmMqTestStep>(keywordsProperties, testStepValueConverter) {

    override fun createTestStepBuilder(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): DefaultTestStep.Builder<IbmMqTestStep> {
        return IbmMqTestStep.Builder()
                .withSendTo(convertValue<String>(rawTestStep[getKeywordName(SEND_TO_KEYWORD, "sendTo")], parameters))
                .withReceiveFrom(convertValue<String>(rawTestStep[getKeywordName(RECEIVE_FROM_KEYWORD, "receiveFrom")], parameters))
                .withRespondTo(convertValue<String>(rawTestStep[getKeywordName(RESPOND_TO_KEYWORD, "respondTo")], parameters))
    }

    override fun isConstructableFrom(rawTestStep: Map<String, Any?>): Boolean {
        return rawTestStep.getOrDefault(getKeywordName(PROTOCOL_KEY_KEYWORD, "protocol"), "") == IBMMQ
    }

    companion object {
        private const val PROTOCOL_KEY_KEYWORD = "test.step.keyword.protocol"
        private const val SEND_TO_KEYWORD = "test.step.keyword.sendTo"
        private const val RECEIVE_FROM_KEYWORD = "test.step.keyword.receiveFrom"
        private const val RESPOND_TO_KEYWORD = "test.step.keyword.respondTo"
        private const val IBMMQ = "ibmmq"
    }
}