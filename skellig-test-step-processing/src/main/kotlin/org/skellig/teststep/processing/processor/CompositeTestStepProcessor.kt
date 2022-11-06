package org.skellig.teststep.processing.processor

import org.skellig.teststep.processing.converter.TestStepResultConverter
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.processor.TestStepProcessor.TestStepRunResult
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.TestStepResultValidator

class CompositeTestStepProcessor private constructor(
    testScenarioState: TestScenarioState,
    testStepResultConverter: TestStepResultConverter,
    testStepResultValidator: TestStepResultValidator
) : TestStepProcessor<TestStep> {

    private val testStepProcessors: MutableList<TestStepProcessor<in TestStep>> = mutableListOf()

    init {
        registerTestStepProcessor(
            DefaultTestStepProcessor.Builder()
                .withTestScenarioState(testScenarioState)
                .withTestStepResultConverter(testStepResultConverter)
                .withValidator(testStepResultValidator)
                .build()
        )
        registerTestStepProcessor(GroupedTestStepProcessor(this))
        registerTestStepProcessor(ClassTestStepProcessor(testScenarioState))
    }

    override fun process(testStep: TestStep): TestStepRunResult {

        val testStepProcessor = testStepProcessors.find { testStep.javaClass == it.getTestStepClass() }

        return testStepProcessor?.let { ts ->
            return ts.process(testStep)
        } ?: throw TestStepProcessingException("No processor was found for test step '${testStep.name}'")
    }

    fun registerTestStepProcessor(testStepProcessor: TestStepProcessor<out TestStep>) {
        testStepProcessors.add(testStepProcessor as TestStepProcessor<TestStep>)
    }

    override fun getTestStepClass(): Class<TestStep> {
        return TestStep::class.java
    }

    override fun close() {
        testStepProcessors.forEach { it.close() }
    }

    class Builder : BaseTestStepProcessor.Builder<TestStep>() {

        override fun build(): CompositeTestStepProcessor {
            return CompositeTestStepProcessor(testScenarioState!!, testStepResultConverter!!, validator!!)
        }
    }
}
