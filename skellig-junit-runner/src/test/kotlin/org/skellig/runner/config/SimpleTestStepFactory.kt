package org.skellig.runner.config

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.factory.BaseDefaultTestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression

class SimpleTestStepFactory(
    testStepRegistry: TestStepRegistry,
    valueExpressionContextFactory: ValueExpressionContextFactory
) : BaseDefaultTestStepFactory<SimpleTestStepFactory.SimpleTestStep>(testStepRegistry, valueExpressionContextFactory) {

    companion object {
        private val CAPTURE_DATA = AlphanumericValueExpression("captureData")
    }

    override fun isConstructableFrom(rawTestStep: Map<ValueExpression, ValueExpression?>): Boolean {
        return rawTestStep.containsKey(CAPTURE_DATA)
    }

    override fun createTestStepBuilder(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): DefaultTestStep.Builder<SimpleTestStep> {
        return SimpleTestStep.Builder()
            .withCaptureData(convertValue<String>(rawTestStep[CAPTURE_DATA], parameters))
    }

    class SimpleTestStep(
        id: String,
        override val name: String,
        val captureData: String
    ) : DefaultTestStep(id = id, name = name) {

        class Builder : DefaultTestStep.Builder<SimpleTestStep>() {

            private var captureData: String? = null

            fun withCaptureData(captureData: String?) = apply {
                this.captureData = captureData
            }

            override fun build(): SimpleTestStep {
                return SimpleTestStep(id = id!!, name = name!!, captureData = captureData!!)
            }
        }
    }
}