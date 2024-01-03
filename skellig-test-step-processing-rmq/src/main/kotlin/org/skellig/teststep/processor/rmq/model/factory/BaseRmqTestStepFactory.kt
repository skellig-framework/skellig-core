package org.skellig.teststep.processor.rmq.model.factory

import org.skellig.teststep.processing.model.factory.BaseDefaultTestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.processor.rmq.model.BaseRmqTestStep
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression

abstract class BaseRmqTestStepFactory<T : BaseRmqTestStep>(
    testStepRegistry: TestStepRegistry,
    valueExpressionContextFactory: ValueExpressionContextFactory,
    defaultTestDataConverter: String? = null
) : BaseDefaultTestStepFactory<T>(testStepRegistry, valueExpressionContextFactory, defaultTestDataConverter) {

    companion object {
        internal val PROTOCOL_KEY_KEYWORD = fromProperty("protocol")
        internal val ROUTING_KEY_KEYWORD = fromProperty("routingKey")
        internal val RESPOND_TO_KEYWORD = fromProperty("respondTo")
        internal val CONSUME_FROM_KEYWORD = fromProperty("consumeFrom")
        internal val RMQ_PROPERTIES_KEYWORD = fromProperty("properties")
        internal val RMQ = fromProperty("rmq")
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