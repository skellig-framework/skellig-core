package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.reader.value.expression.ValueExpression

class CompositeTestStepFactory private constructor(
    testStepsRegistry: TestStepRegistry,
    valueExpressionContextFactory: ValueExpressionContextFactory
) : TestStepFactory<TestStep> {

    private val factories: MutableList<TestStepFactory<out TestStep>> = mutableListOf()
    private var defaultTestStepFactory: TestStepFactory<DefaultTestStep>

    init {
        registerTestStepFactory(ClassTestStepFactory())
        registerTestStepFactory(TaskTestStepFactory(testStepsRegistry, valueExpressionContextFactory))

        defaultTestStepFactory = DefaultTestStepFactory.Builder()
            .withValueExpressionContextFactory(valueExpressionContextFactory)
            .withTestStepRegistry(testStepsRegistry)
            .build()
    }

    fun registerTestStepFactory(factory: TestStepFactory<out TestStep>) {
        factories.add(factory)
    }

    override fun create(testStepName: String, rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): TestStep {
        val factory = factories.firstOrNull { it.isConstructableFrom(rawTestStep) } ?: defaultTestStepFactory
        return factory.create(testStepName, rawTestStep, parameters)
    }

    override fun isConstructableFrom(rawTestStep: Map<ValueExpression, ValueExpression?>): Boolean {
        return true
    }

    class Builder {

        private var testStepsRegistry: TestStepRegistry? = null
        private var valueExpressionContextFactory: ValueExpressionContextFactory? = null

        fun withValueExpressionContextFactory(valueExpressionContextFactory: ValueExpressionContextFactory?) =
            apply { this.valueExpressionContextFactory = valueExpressionContextFactory }

        fun withTestDataRegistry(testStepsRegistry: TestStepRegistry) =
            apply { this.testStepsRegistry = testStepsRegistry }

        fun build(): CompositeTestStepFactory {
            return CompositeTestStepFactory(
                testStepsRegistry!!,
                valueExpressionContextFactory ?: error("ValueExpressionContextFactory is mandatory for DefaultTestStepFactory")
            )
        }

    }
}