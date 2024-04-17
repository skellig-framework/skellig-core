package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.processor.CompositeTestStepProcessor
import org.skellig.teststep.processing.util.debug
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.reader.value.expression.ValueExpression

/**
 * The main factory class for creating test steps. It has other instances of [TestStepFactory] and
 * decides which one to apply by calling [TestStepFactory.isConstructableFrom] method on a raw test step.
 * It registers some internal Test Step factories when object is created. The other [TestStepFactory] can be registered by
 * calling [CompositeTestStepFactory.registerTestStepFactory] method.
 *
 * @property testStepsRegistry The registry [TestStepRegistry] for storing and retrieving test steps.
 * @property valueExpressionContextFactory The factory for creating value expression contexts for evaluating [ValueExpression].
 */
class CompositeTestStepFactory private constructor(
    testStepsRegistry: TestStepRegistry,
    valueExpressionContextFactory: ValueExpressionContextFactory
) : TestStepFactory<TestStep> {

    private val log = logger<CompositeTestStepProcessor>()
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

    /**
     * Registers a TestStepFactory for creating TestStep objects.
     *
     * @param factory The [TestStepFactory] to be registered.
     * @see TestStepFactory
     */
    fun registerTestStepFactory(factory: TestStepFactory<out TestStep>) {
        log.debug {"Register Test Step Factory class '${factory.javaClass.simpleName}'" }
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