package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.reader.value.expression.ValueExpression


/**
 * This class is responsible for creating instances of the [DefaultTestStep] class.
 * It extends the [BaseDefaultTestStepFactory] class and implements the [TestStepFactory] interface
 * with the [DefaultTestStep] type parameter.
 *
 * @param testStepRegistry The test step registry used to store and retrieve test steps.
 * @param valueExpressionContextFactory The factory used to create instances of `ValueExpressionContext`.
 */
internal class DefaultTestStepFactory(
    testStepRegistry: TestStepRegistry,
    valueExpressionContextFactory: ValueExpressionContextFactory
) : BaseDefaultTestStepFactory<DefaultTestStep>(testStepRegistry, valueExpressionContextFactory) {

    override fun createTestStepBuilder(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): DefaultTestStep.Builder<DefaultTestStep> {
        return DefaultTestStep.DefaultTestStepBuilder()
    }

    override fun isConstructableFrom(rawTestStep: Map<ValueExpression, ValueExpression?>): Boolean {
        return true
    }

    class Builder {
        private var valueExpressionContextFactory: ValueExpressionContextFactory? = null
        private var testStepRegistry: TestStepRegistry? = null

        fun withValueExpressionContextFactory(valueExpressionContextFactory: ValueExpressionContextFactory?) =
            apply { this.valueExpressionContextFactory = valueExpressionContextFactory }

        fun withTestStepRegistry(testStepRegistry: TestStepRegistry?) =
            apply { this.testStepRegistry = testStepRegistry }

        fun build(): TestStepFactory<DefaultTestStep> {
            return DefaultTestStepFactory(
                testStepRegistry ?: error("TestStepRegistry is mandatory for DefaultTestStepFactory"),
                valueExpressionContextFactory ?: error("ValueExpressionContextFactory is mandatory for DefaultTestStepFactory"),
            )
        }
    }
}