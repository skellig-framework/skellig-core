package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.model.DefaultTestStep
import java.util.*


internal class DefaultTestStepFactory(
    testStepRegistry: TestStepRegistry,
    keywordsProperties: Properties?,
    testStepFactoryValueConverter: TestStepFactoryValueConverter)
    : BaseDefaultTestStepFactory<DefaultTestStep>(testStepRegistry, keywordsProperties, testStepFactoryValueConverter) {

    override fun createTestStepBuilder(rawTestStep: Map<Any, Any?>, parameters: Map<String, Any?>): DefaultTestStep.Builder<DefaultTestStep> {
        return DefaultTestStep.DefaultTestStepBuilder()
    }

    override fun isConstructableFrom(rawTestStep: Map<Any, Any?>): Boolean {
        return true
    }

    class Builder {
        private var keywordsProperties: Properties? = null
        private var testStepFactoryValueConverter: TestStepFactoryValueConverter? = null
        private var testStepRegistry: TestStepRegistry? = null;

        fun withKeywordsProperties(keywordsProperties: Properties?) =
            apply { this.keywordsProperties = keywordsProperties }

        fun withTestStepValueConverter(testStepFactoryValueConverter: TestStepFactoryValueConverter?) =
            apply { this.testStepFactoryValueConverter = testStepFactoryValueConverter }

        fun withTestStepRegistry(testStepRegistry: TestStepRegistry?) =
            apply { this.testStepRegistry = testStepRegistry }

        fun build(): TestStepFactory<DefaultTestStep> {
            return DefaultTestStepFactory(
                testStepRegistry ?: error("TestStepRegistry is mandatory for DefaultTestStepFactory"),
                keywordsProperties,
                testStepFactoryValueConverter ?: error("TestStepFactoryValueConverter is mandatory for DefaultTestStepFactory")
            )
        }
    }
}