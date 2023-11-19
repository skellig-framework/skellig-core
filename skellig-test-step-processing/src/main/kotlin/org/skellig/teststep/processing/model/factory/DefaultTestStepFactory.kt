package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import java.util.*


internal class DefaultTestStepFactory(
    testStepRegistry: TestStepRegistry,
    keywordsProperties: Properties?,
    testStepFactoryValueConverter: TestStepFactoryValueConverter,
    valueExpressionContextFactory: ValueExpressionContextFactory
) : BaseDefaultTestStepFactory<DefaultTestStep>(testStepRegistry, keywordsProperties, testStepFactoryValueConverter, valueExpressionContextFactory) {

    override fun createTestStepBuilder(rawTestStep: Map<Any, Any?>, parameters: Map<String, Any?>): DefaultTestStep.Builder<DefaultTestStep> {
        return DefaultTestStep.DefaultTestStepBuilder()
    }

    override fun isConstructableFrom(rawTestStep: Map<Any, Any?>): Boolean {
        return true
    }

    class Builder {
        private var keywordsProperties: Properties? = null
        private var testStepFactoryValueConverter: TestStepFactoryValueConverter? = null
        private var valueExpressionContextFactory: ValueExpressionContextFactory? = null
        private var testStepRegistry: TestStepRegistry? = null;

        fun withKeywordsProperties(keywordsProperties: Properties?) =
            apply { this.keywordsProperties = keywordsProperties }

        @Deprecated("use withValueExpressionContextFactory")
        fun withTestStepValueConverter(testStepFactoryValueConverter: TestStepFactoryValueConverter?) =
            apply { this.testStepFactoryValueConverter = testStepFactoryValueConverter }

        fun withValueExpressionContextFactory(valueExpressionContextFactory: ValueExpressionContextFactory?) =
            apply { this.valueExpressionContextFactory = valueExpressionContextFactory }

        fun withTestStepRegistry(testStepRegistry: TestStepRegistry?) =
            apply { this.testStepRegistry = testStepRegistry }

        fun build(): TestStepFactory<DefaultTestStep> {
            return DefaultTestStepFactory(
                testStepRegistry ?: error("TestStepRegistry is mandatory for DefaultTestStepFactory"),
                keywordsProperties,
                testStepFactoryValueConverter ?: error("TestStepFactoryValueConverter is mandatory for DefaultTestStepFactory"),
                valueExpressionContextFactory ?: error("ValueExpressionContextFactory is mandatory for DefaultTestStepFactory"),
            )
        }
    }
}