package org.skellig.runner.config

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.factory.BaseDefaultTestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression

class SimpleMessageTestStepFactory(
    testStepRegistry: TestStepRegistry,
    valueExpressionContextFactory: ValueExpressionContextFactory,
    defaultTestDataConverter: String? = null
) : BaseDefaultTestStepFactory<SimpleMessageTestStep>(testStepRegistry, valueExpressionContextFactory, defaultTestDataConverter) {

    companion object {
        private val RECEIVER = fromProperty("receiver")
        private val RECEIVE_FROM = fromProperty("receiveFrom")
    }

    override fun createTestStepBuilder(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): DefaultTestStep.Builder<SimpleMessageTestStep> {
        return SimpleMessageTestStep.Builder()
            .withReceiver(convertValue(rawTestStep[RECEIVER], parameters))
            .withReceiveFrom(convertValue(rawTestStep[RECEIVE_FROM], parameters))
    }

    override fun isConstructableFrom(rawTestStep: Map<ValueExpression, ValueExpression?>): Boolean {
        return rawTestStep.containsKey(RECEIVER) || rawTestStep.containsKey(RECEIVE_FROM)
    }
}