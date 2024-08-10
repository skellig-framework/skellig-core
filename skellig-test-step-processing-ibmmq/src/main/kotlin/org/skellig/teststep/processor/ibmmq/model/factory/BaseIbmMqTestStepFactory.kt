package org.skellig.teststep.processor.ibmmq.model.factory

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.factory.BaseDefaultTestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression

abstract class BaseIbmMqTestStepFactory<T : DefaultTestStep>(
    testStepRegistry: TestStepRegistry,
    valueExpressionContextFactory: ValueExpressionContextFactory,
    defaultTestDataConverter: String? = null
) : BaseDefaultTestStepFactory<T>(testStepRegistry, valueExpressionContextFactory, defaultTestDataConverter) {

    companion object {
        protected val PROTOCOL_KEY_KEYWORD = fromProperty("protocol")
        internal val RESPOND_TO_KEYWORD = fromProperty("respondTo")
        protected val CONSUME_FROM_KEYWORD = fromProperty("consumeFrom")
        protected val IBMMQ = fromProperty("ibmmq")

    }

    internal fun getConsumeFromKeyword() = CONSUME_FROM_KEYWORD

    internal fun hasIbmMqRequiredData(rawTestStep: Map<ValueExpression, ValueExpression?>): Boolean =
        rawTestStep.getOrDefault(PROTOCOL_KEY_KEYWORD, "") == IBMMQ
}