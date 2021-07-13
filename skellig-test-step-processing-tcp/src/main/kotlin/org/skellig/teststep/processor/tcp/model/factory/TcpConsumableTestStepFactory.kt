package org.skellig.teststep.processor.tcp.model.factory

import org.skellig.teststep.processing.converter.TestStepValueConverter
import org.skellig.teststep.processor.tcp.model.BaseTcpTestStep
import org.skellig.teststep.processor.tcp.model.TcpConsumableTestStep
import java.util.*

class TcpConsumableTestStepFactory(keywordsProperties: Properties?,
                                   testStepValueConverter: TestStepValueConverter?)
    : BaseTcpTestStepFactory<TcpConsumableTestStep>(keywordsProperties, testStepValueConverter) {

    override fun createTestStepBuilder(rawTestStep: Map<String, Any?>,
                                       parameters: Map<String, Any?>): BaseTcpTestStep.Builder<TcpConsumableTestStep> {
        return TcpConsumableTestStep.Builder()
            .consumeFrom(getConsumeFromChannels(rawTestStep, parameters))
            .respondTo(getRespondToChannels(rawTestStep, parameters))
            .readBufferSize(getReadBufferSize(rawTestStep, parameters))
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
        rawTestStep.containsKey(getConsumeFromKeyword()) && super.isConstructableFrom(rawTestStep)
}