package org.skellig.teststep.processor.ibmmq.model.factory

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.factory.BaseDefaultTestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepFactoryValueConverter
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import java.util.*

abstract class BaseIbmMqTestStepFactory<T : DefaultTestStep>(
    testStepRegistry: TestStepRegistry,
    keywordsProperties: Properties?,
    testStepFactoryValueConverter: TestStepFactoryValueConverter)
    : BaseDefaultTestStepFactory<T>(testStepRegistry, keywordsProperties, testStepFactoryValueConverter) {

    companion object {
        protected const val PROTOCOL_KEY_KEYWORD = "test.step.keyword.protocol"
        internal const val RESPOND_TO_KEYWORD = "test.step.keyword.respondTo"
        protected const val CONSUME_FROM_KEYWORD = "test.step.keyword.consumeFrom"
        protected const val IBMMQ = "ibmmq"

    }

    private fun toSet(channel: Any?): Set<String>? {
        return when (channel) {
            is Collection<*> -> channel.map { it.toString() }.toSet()
            else -> channel?.let { setOf(channel.toString()) }
        }
    }

    internal fun getConsumeFromKeyword() = getKeywordName(CONSUME_FROM_KEYWORD, "consumeFrom")

    internal fun hasIbmMqRequiredData(rawTestStep: Map<Any, Any?>): Boolean =
        rawTestStep.getOrDefault(getKeywordName(PROTOCOL_KEY_KEYWORD, "protocol"), "") == IBMMQ
}