package org.skellig.teststep.processor.wiremock.model.factory

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.factory.BaseDefaultTestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.processor.wiremock.model.WireMockTestStep
import org.skellig.teststep.reader.value.expression.ValueExpression

/** Recognises no steps yet, so it never competes with other factories such as HTTP. */
internal class WireMockTestStepFactory(
    registry: TestStepRegistry,
    expressions: ValueExpressionContextFactory
) : BaseDefaultTestStepFactory<WireMockTestStep>(registry, expressions) {

    override fun isConstructableFrom(rawTestStep: Map<ValueExpression, ValueExpression?>) = false

    override fun createTestStepBuilder(
        rawTestStep: Map<ValueExpression, ValueExpression?>,
        parameters: Map<String, Any?>
    ): DefaultTestStep.Builder<WireMockTestStep> = WireMockTestStep.Builder()
}
