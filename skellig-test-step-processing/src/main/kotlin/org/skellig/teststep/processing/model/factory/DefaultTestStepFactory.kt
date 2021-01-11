package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.converter.TestDataConverter
import org.skellig.teststep.processing.converter.TestStepValueConverter
import org.skellig.teststep.processing.model.DefaultTestStep
import java.util.*


internal class DefaultTestStepFactory(keywordsProperties: Properties?,
                             testStepValueConverter: TestStepValueConverter?,
                             testDataConverter: TestDataConverter?)
    : BaseTestStepFactory<DefaultTestStep>(keywordsProperties, testStepValueConverter, testDataConverter) {

    override fun createTestStepBuilder(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): DefaultTestStep.Builder<DefaultTestStep> {
        return DefaultTestStep.DefaultTestStepBuilder()
    }

    override fun isConstructableFrom(rawTestStep: Map<String, Any?>): Boolean {
        return true
    }

    class Builder {
        private var keywordsProperties: Properties? = null
        private var testStepValueConverter: TestStepValueConverter? = null
        private var testDataConverter: TestDataConverter? = null

        fun withKeywordsProperties(keywordsProperties: Properties?) =
                apply { this.keywordsProperties = keywordsProperties }

        fun withTestStepValueConverter(testStepValueConverter: TestStepValueConverter?) =
                apply { this.testStepValueConverter = testStepValueConverter }

        fun withTestDataConverter(testDataConverter: TestDataConverter?) =
                apply { this.testDataConverter = testDataConverter }

        fun build(): TestStepFactory<DefaultTestStep> {
            return DefaultTestStepFactory(keywordsProperties, testStepValueConverter, testDataConverter)
        }
    }
}