package org.skellig.teststep.processing.processor

import org.skellig.teststep.processing.converter.TestStepResultConverter
import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.processor.TestStepProcessor.TestStepRunResult
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.TestStepResultValidator
import java.util.*
import java.util.function.Consumer

class DefaultTestStepProcessor private constructor(
        private val testStepProcessors: List<TestStepProcessor<in TestStep>>,
        testScenarioState: TestScenarioState,
        validator: TestStepResultValidator,
        testStepResultConverter: TestStepResultConverter?)
    : ValidatableTestStepProcessor<TestStep>(testScenarioState, validator, testStepResultConverter) {

    override fun process(testStep: TestStep): TestStepRunResult {

        val testStepProcessor = testStepProcessors
                .find { testStep.javaClass == it.getTestStepClass() }

        return testStepProcessor?.let { ts ->
            return ts.process(testStep)
        } ?: run {
            val testStepRunResult = TestStepRunResult(testStep)
            testScenarioState.set(testStep.getId, testStep)
            validate(testStep, testStepRunResult)

            return testStepRunResult
        }
    }

    private fun validate(testStep: TestStep, testStepRunResult: TestStepRunResult) {
        var error: RuntimeException? = null
        try {
            testStep.validationDetails?.let { super.validate(testStep) }
        } catch (ex: ValidationException) {
            error = ex
        } finally {
            testStepRunResult.notify(null, error)
        }
    }

    override fun getTestStepClass(): Class<TestStep> {
        return TestStep::class.java
    }

    override fun close() {
        testStepProcessors.forEach(Consumer { obj: TestStepProcessor<*> -> obj.close() })
    }

    class Builder : BaseTestStepProcessor.Builder<TestStep>() {

        private val testStepProcessors = mutableListOf<TestStepProcessor<in TestStep>>()

        fun withTestStepProcessor(testStepProcessor: TestStepProcessor<out TestStep>) = apply {
            testStepProcessors.add(testStepProcessor as TestStepProcessor<TestStep>)
        }

        override fun build(): TestStepProcessor<TestStep> {
            Objects.requireNonNull(testScenarioState, "TestScenarioState must be provided")
            Objects.requireNonNull(validator, "Validator must be provided")

            return DefaultTestStepProcessor(testStepProcessors, testScenarioState!!, validator!!, testStepResultConverter)
        }

    }

}
