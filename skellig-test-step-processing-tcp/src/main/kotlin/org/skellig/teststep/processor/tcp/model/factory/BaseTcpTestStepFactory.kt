package org.skellig.teststep.processor.tcp.model.factory

import org.skellig.teststep.processing.model.factory.BaseDefaultTestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.processor.tcp.model.BaseTcpTestStep
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression

abstract class BaseTcpTestStepFactory<T : BaseTcpTestStep>(
    testStepRegistry: TestStepRegistry,
    valueExpressionContextFactory: ValueExpressionContextFactory
) : BaseDefaultTestStepFactory<T>(testStepRegistry, valueExpressionContextFactory) {

    companion object {
        internal val RESPOND_TO_KEYWORD = AlphanumericValueExpression("respondTo")
        internal val PROTOCOL_KEY_KEYWORD = AlphanumericValueExpression("protocol")
        internal val CONSUME_FROM_KEYWORD = AlphanumericValueExpression("consumeFrom")
        internal val BUFFER_SIZE_KEYWORD = AlphanumericValueExpression("bufferSize")
        internal const val TCP = "tcp"
    }

    protected fun getReadBufferSize(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): Int? {
        val readBufferSize = rawTestStep[BUFFER_SIZE_KEYWORD]
        return convertValue(readBufferSize, parameters)
    }

    override fun isConstructableFrom(rawTestStep: Map<ValueExpression, ValueExpression?>): Boolean {
        return rawTestStep.getOrDefault(PROTOCOL_KEY_KEYWORD, "") == TCP
    }

    internal fun getConsumeFromKeyword() = CONSUME_FROM_KEYWORD
}