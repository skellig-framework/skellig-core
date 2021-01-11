package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.converter.TestDataConverter
import org.skellig.teststep.processing.converter.TestStepValueConverter
import org.skellig.teststep.processing.model.TestStep
import java.util.*


class CompositeTestStepFactory(var factories: Collection<TestStepFactory<out TestStep>>) : TestStepFactory<TestStep> {

    override fun create(testStepName: String, rawTestStep: Map<String, Any?>, parameters: Map<String, String?>): TestStep {
        return factories
                .firstOrNull { it.isConstructableFrom(rawTestStep) }
                .let { it?.create(testStepName, rawTestStep, parameters) }
                ?: throw IllegalArgumentException("Cannot construct Test Step as no factory was found for it: ${rawTestStep}")
    }

    override fun isConstructableFrom(rawTestStep: Map<String, Any?>): Boolean {
        return true
    }

    class Builder {

        private lateinit var testStepsRegistry: TestStepRegistry
        private val testStepFactories = mutableListOf<TestStepFactory<out TestStep>>()
        private var keywordsProperties: Properties? = null
        private var testStepValueConverter: TestStepValueConverter? = null
        private var testDataConverter: TestDataConverter? = null

        fun withTestStepFactory(factory: TestStepFactory<out TestStep>) =
                apply { testStepFactories.add(factory) }

        fun withKeywordsProperties(keywordsProperties: Properties?) =
                apply { this.keywordsProperties = keywordsProperties }

        fun withTestStepValueConverter(testStepValueConverter: TestStepValueConverter?) =
                apply { this.testStepValueConverter = testStepValueConverter }

        fun withTestDataConverter(testDataConverter: TestDataConverter?) =
                apply { this.testDataConverter = testDataConverter }

        fun withTestDataRegistry(testStepsRegistry: TestStepRegistry) =
                apply {this.testStepsRegistry = testStepsRegistry }

        fun build(): TestStepFactory<TestStep> {
            val compositeTestStepFactory = CompositeTestStepFactory(testStepFactories)
            withTestStepFactory(GroupedTestStepFactory(testStepsRegistry, compositeTestStepFactory))
            withTestStepFactory(ClassTestStepFactory())
            withTestStepFactory(DefaultTestStepFactory.Builder()
                    .withKeywordsProperties(keywordsProperties)
                    .withTestStepValueConverter(testStepValueConverter)
                    .withTestDataConverter(testDataConverter)
                    .build())


            return compositeTestStepFactory
        }

    }
}