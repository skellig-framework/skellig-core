package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.TestStep
import java.util.*


class CompositeTestStepFactory private constructor(
    testStepsRegistry: TestStepRegistry,
    keywordsProperties: Properties?,
    testStepFactoryValueConverter: TestStepFactoryValueConverter
) : TestStepFactory<TestStep> {

    private val factories: MutableList<TestStepFactory<out TestStep>> = mutableListOf()
    private var defaultTestStepFactory: TestStepFactory<DefaultTestStep>

    init {
        registerTestStepFactory(GroupedTestStepFactory(testStepsRegistry, this, keywordsProperties, testStepFactoryValueConverter))
        registerTestStepFactory(ClassTestStepFactory())

        defaultTestStepFactory = DefaultTestStepFactory.Builder()
            .withKeywordsProperties(keywordsProperties)
            .withTestStepValueConverter(testStepFactoryValueConverter)
            .withTestStepRegistry(testStepsRegistry)
            .build()
    }

    fun registerTestStepFactory(factory: TestStepFactory<out TestStep>) {
        factories.add(factory)
    }

    override fun create(testStepName: String, rawTestStep: Map<Any, Any?>, parameters: Map<String, String?>): TestStep {
        val factory = factories.firstOrNull { it.isConstructableFrom(rawTestStep) } ?: defaultTestStepFactory
        return factory.create(testStepName, rawTestStep, parameters)
    }

    override fun isConstructableFrom(rawTestStep: Map<Any, Any?>): Boolean {
        return true
    }

    class Builder {

        private var testStepsRegistry: TestStepRegistry? = null
        private var keywordsProperties: Properties? = null
        private var testStepFactoryValueConverter: TestStepFactoryValueConverter? = null

        fun withKeywordsProperties(keywordsProperties: Properties?) =
            apply { this.keywordsProperties = keywordsProperties }

        fun withTestStepFactoryValueConverter(testStepFactoryValueConverter: TestStepFactoryValueConverter) =
            apply { this.testStepFactoryValueConverter = testStepFactoryValueConverter }

        fun withTestDataRegistry(testStepsRegistry: TestStepRegistry) =
            apply { this.testStepsRegistry = testStepsRegistry }

        fun build(): CompositeTestStepFactory {
            return CompositeTestStepFactory(testStepsRegistry!!, keywordsProperties, testStepFactoryValueConverter!!)
        }

    }
}