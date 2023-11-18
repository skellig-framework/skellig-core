package org.skellig.teststep.processor.tcp.model.factory

import org.skellig.teststep.processing.model.factory.TestStepFactoryValueConverter
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processor.tcp.model.BaseTcpTestStep
import org.skellig.teststep.processor.tcp.model.TcpConsumableTestStep
import java.util.*

class TcpConsumableTestStepFactory(testStepRegistry: TestStepRegistry,
                                   keywordsProperties: Properties?,
                                   testStepFactoryValueConverter: TestStepFactoryValueConverter)
    : BaseTcpTestStepFactory<TcpConsumableTestStep>(testStepRegistry, keywordsProperties, testStepFactoryValueConverter) {

    override fun createTestStepBuilder(rawTestStep: Map<Any, Any?>,
                                       parameters: Map<String, Any?>): BaseTcpTestStep.Builder<TcpConsumableTestStep> {
        return TcpConsumableTestStep.Builder()
            .consumeFrom(getConsumeFromChannels(rawTestStep, parameters))
            .respondTo(getRespondToChannels(rawTestStep, parameters))
            .readBufferSize(getReadBufferSize(rawTestStep, parameters))
    }

    private fun getRespondToChannels(rawTestStep: Map<Any, Any?>, parameters: Map<String, Any?>): List<String>? =
        toList(convertValue<Any>(rawTestStep[getKeywordName(RESPOND_TO_KEYWORD, "respondTo")], parameters))

    private fun getConsumeFromChannels(rawTestStep: Map<Any, Any?>, parameters: Map<String, Any?>): List<String>? =
        toList(convertValue<Any>(rawTestStep[getConsumeFromKeyword()], parameters))

    private fun toList(channel: Any?): List<String>? {
        return when (channel) {
            is Collection<*> -> channel.map { it.toString() }.toList()
            else -> channel?.let { listOf(channel.toString()) }
        }
    }

    override fun isConstructableFrom(rawTestStep: Map<Any, Any?>): Boolean =
        rawTestStep.containsKey(getConsumeFromKeyword()) && super.isConstructableFrom(rawTestStep)
}