package org.skellig.teststep.processor.tcp.model.factory

import org.skellig.teststep.processing.model.factory.BaseDefaultTestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepFactoryValueConverter
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processor.tcp.model.BaseTcpTestStep
import java.util.*

abstract class BaseTcpTestStepFactory<T : BaseTcpTestStep>(testStepRegistry: TestStepRegistry,
                                                           keywordsProperties: Properties?,
                                                           testStepFactoryValueConverter: TestStepFactoryValueConverter)
    : BaseDefaultTestStepFactory<T>(testStepRegistry, keywordsProperties, testStepFactoryValueConverter) {

    companion object {
        internal const val RESPOND_TO_KEYWORD = "test.step.keyword.respondTo"
        internal const val PROTOCOL_KEY_KEYWORD = "test.step.keyword.protocol"
        internal const val CONSUME_FROM_KEYWORD = "test.step.keyword.consumeFrom"
        internal const val BUFFER_SIZE_KEYWORD = "test.step.keyword.bufferSize"
        internal const val TCP = "tcp"
    }

    protected fun getReadBufferSize(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>) : Int? {
        val readBufferSize = rawTestStep[getKeywordName(BUFFER_SIZE_KEYWORD, "bufferSize")]
        return convertValue(readBufferSize, parameters)
    }

    override fun isConstructableFrom(rawTestStep: Map<String, Any?>): Boolean {
        return rawTestStep.getOrDefault(getKeywordName(PROTOCOL_KEY_KEYWORD, "protocol"), "") == TCP
    }

    internal fun getConsumeFromKeyword() = getKeywordName(CONSUME_FROM_KEYWORD, "consumeFrom")
}