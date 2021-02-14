package org.skellig.teststep.processing.processor

import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.processor.TestStepProcessor.TestStepRunResult

class CompositeTestStepProcessor private constructor(
        private val testStepProcessors: List<TestStepProcessor<in TestStep>>) : TestStepProcessor<TestStep> {

    override fun process(testStep: TestStep): TestStepRunResult {

        val testStepProcessor = testStepProcessors.find { testStep.javaClass == it.getTestStepClass() }

        return testStepProcessor?.let { ts ->
            return ts.process(testStep)
        } ?: throw TestStepProcessingException("No processor was found for test step '${testStep.name}'")
    }

    override fun getTestStepClass(): Class<TestStep> {
        return TestStep::class.java
    }

    override fun close() {
        testStepProcessors.forEach { it.close() }
    }

    class Builder : BaseTestStepProcessor.Builder<TestStep>() {

        private val testStepProcessors = mutableListOf<TestStepProcessor<in TestStep>>()

        fun withTestStepProcessor(testStepProcessor: TestStepProcessor<out TestStep>) = apply {
            testStepProcessors.add(testStepProcessor as TestStepProcessor<TestStep>)
        }

        override fun build(): TestStepProcessor<TestStep> {
            withTestStepProcessor(DefaultTestStepProcessor.Builder()
                    .withTestScenarioState(testScenarioState)
                    .withTestStepResultConverter(testStepResultConverter)
                    .withValidator(validator)
                    .build())
            val compositeTestStepProcessor = CompositeTestStepProcessor(testStepProcessors)
            withTestStepProcessor(GroupedTestStepProcessor(compositeTestStepProcessor))
            withTestStepProcessor(ClassTestStepProcessor())

            return compositeTestStepProcessor
        }
    }
}
