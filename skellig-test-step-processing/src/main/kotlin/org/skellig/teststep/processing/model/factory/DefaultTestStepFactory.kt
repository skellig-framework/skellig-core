package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.model.DefaultTestStep
import java.util.*


internal class DefaultTestStepFactory(keywordsProperties: Properties?,
                                      testStepFactoryValueConverter: TestStepFactoryValueConverter)
    : BaseDefaultTestStepFactory<DefaultTestStep>(keywordsProperties, testStepFactoryValueConverter) {

    override fun createTestStepBuilder(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): DefaultTestStep.Builder<DefaultTestStep> {
        return DefaultTestStep.DefaultTestStepBuilder()
    }

    override fun isConstructableFrom(rawTestStep: Map<String, Any?>): Boolean {
        return true
    }

    class Builder {
        private var keywordsProperties: Properties? = null
        private var testStepFactoryValueConverter: TestStepFactoryValueConverter? = null

        fun withKeywordsProperties(keywordsProperties: Properties?) =
                apply { this.keywordsProperties = keywordsProperties }

        fun withTestStepValueConverter(testStepFactoryValueConverter: TestStepFactoryValueConverter?) =
                apply { this.testStepFactoryValueConverter = testStepFactoryValueConverter }

        fun build(): TestStepFactory<DefaultTestStep> {
            return DefaultTestStepFactory(keywordsProperties, testStepFactoryValueConverter!!)
        }
    }
}