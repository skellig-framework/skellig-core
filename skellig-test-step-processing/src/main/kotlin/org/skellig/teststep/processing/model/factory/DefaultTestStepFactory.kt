package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.converter.TestDataConverter
import org.skellig.teststep.processing.converter.TestStepValueConverter
import org.skellig.teststep.processing.model.TestStep
import java.util.*


class DefaultTestStepFactory(keywordsProperties: Properties?,
                             testStepValueConverter: TestStepValueConverter?,
                             var factories: Collection<TestStepFactory>,
                             testDataConverter: TestDataConverter?)
    : BaseTestStepFactory(keywordsProperties, testStepValueConverter, testDataConverter) {

    override fun create(testStepName: String, rawTestStep: Map<String, Any?>, parameters: Map<String, String?>): TestStep {
        return factories
                .firstOrNull { it.isConstructableFrom(rawTestStep) }
                .let { it?.create(testStepName, rawTestStep, parameters) }
                ?: super.create(testStepName, rawTestStep, parameters)
    }

    override fun createTestStepBuilder(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): TestStep.Builder {
        return TestStep.Builder()
    }

    override fun isConstructableFrom(rawTestStep: Map<String, Any?>): Boolean {
        return true
    }

    class Builder {
        private val testStepFactories = mutableListOf<TestStepFactory>()
        private var keywordsProperties: Properties? = null
        private var testStepValueConverter: TestStepValueConverter? = null
        private var testDataConverter: TestDataConverter? = null

        fun withTestStepFactory(factory: TestStepFactory) =
                apply { testStepFactories.add(factory) }

        fun withKeywordsProperties(keywordsProperties: Properties?) =
                apply { this.keywordsProperties = keywordsProperties }

        fun withTestStepValueConverter(testStepValueConverter: TestStepValueConverter?) =
                apply { this.testStepValueConverter = testStepValueConverter }

        fun withTestDataConverter(testDataConverter: TestDataConverter?) =
                apply { this.testDataConverter = testDataConverter }

        fun build(): TestStepFactory {
            return DefaultTestStepFactory(keywordsProperties, testStepValueConverter,
                    testStepFactories, testDataConverter)
        }
    }
}