package org.skellig.teststep.processor.rmq.model.factory

import org.skellig.teststep.processing.model.factory.BaseDefaultTestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.processor.rmq.model.BaseRmqTestStep
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression

abstract class BaseRmqTestStepFactory<T : BaseRmqTestStep>(
    testStepRegistry: TestStepRegistry,
    valueExpressionContextFactory: ValueExpressionContextFactory
) : BaseDefaultTestStepFactory<T>(testStepRegistry, valueExpressionContextFactory) {

    companion object {
        internal val PROTOCOL_KEY_KEYWORD = AlphanumericValueExpression("protocol")
        internal val ROUTING_KEY_KEYWORD = AlphanumericValueExpression("routingKey")
        internal val RESPOND_TO_KEYWORD = AlphanumericValueExpression("respondTo")
        internal val CONSUME_FROM_KEYWORD = AlphanumericValueExpression("consumeFrom")
        internal val RMQ_PROPERTIES_KEYWORD = AlphanumericValueExpression("properties")
        internal val RMQ = "rmq"
    }

    protected fun getRoutingKey(rawTestStep: Map<ValueExpression, ValueExpression?>): ValueExpression? =
        rawTestStep[ROUTING_KEY_KEYWORD]

    protected fun getProperties(rawTestStep: Map<ValueExpression, ValueExpression?>): ValueExpression? =
        rawTestStep[RMQ_PROPERTIES_KEYWORD]

    internal fun hasRmqRequiredData(rawTestStep: Map<ValueExpression, ValueExpression?>): Boolean =
        getRoutingKey(rawTestStep) != null ||
                rawTestStep.getOrDefault(PROTOCOL_KEY_KEYWORD, "") == RMQ

    internal fun getConsumeFromKeyword() = CONSUME_FROM_KEYWORD
}