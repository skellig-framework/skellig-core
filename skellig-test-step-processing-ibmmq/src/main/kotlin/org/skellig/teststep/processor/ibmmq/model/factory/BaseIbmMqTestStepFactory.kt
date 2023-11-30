package org.skellig.teststep.processor.ibmmq.model.factory

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.factory.BaseDefaultTestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression

abstract class BaseIbmMqTestStepFactory<T : DefaultTestStep>(
    testStepRegistry: TestStepRegistry,
    valueExpressionContextFactory: ValueExpressionContextFactory
) : BaseDefaultTestStepFactory<T>(testStepRegistry, valueExpressionContextFactory) {

    companion object {
        protected val PROTOCOL_KEY_KEYWORD = AlphanumericValueExpression("protocol")
        internal val RESPOND_TO_KEYWORD = AlphanumericValueExpression("respondTo")
        protected val CONSUME_FROM_KEYWORD = AlphanumericValueExpression("consumeFrom")
        protected const val IBMMQ = "ibmmq"

    }

    private fun toSet(channel: Any?): Set<String>? {
        return when (channel) {
            is Collection<*> -> channel.map { it.toString() }.toSet()
            else -> channel?.let { setOf(channel.toString()) }
        }
    }

    internal fun getConsumeFromKeyword() = CONSUME_FROM_KEYWORD

    internal fun hasIbmMqRequiredData(rawTestStep: Map<ValueExpression, ValueExpression?>): Boolean =
        rawTestStep.getOrDefault(PROTOCOL_KEY_KEYWORD, "") == IBMMQ
}