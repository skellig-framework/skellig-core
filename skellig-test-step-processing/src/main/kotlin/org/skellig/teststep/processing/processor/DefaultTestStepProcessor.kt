package org.skellig.teststep.processing.processor

import org.skellig.teststep.processing.converter.TestStepResultConverter
import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.processor.TestStepProcessor.TestStepRunResult
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.TestStepResultValidator

internal class DefaultTestStepProcessor private constructor(
        testScenarioState: TestScenarioState,
        validator: TestStepResultValidator,
        testStepResultConverter: TestStepResultConverter?)
    : ValidatableTestStepProcessor<DefaultTestStep>(testScenarioState, validator, testStepResultConverter) {

    override fun process(testStep: DefaultTestStep): TestStepRunResult {
        val testStepRunResult = TestStepRunResult(testStep)
        testScenarioState.set(testStep.getId, testStep)
        validate(testStep, testStepRunResult)

        return testStepRunResult
    }

    private fun validate(testStep: DefaultTestStep, testStepRunResult: TestStepRunResult) {
        var error: RuntimeException? = null
        try {
            testStep.validationDetails?.let { super.validate(testStep) }
        } catch (ex: ValidationException) {
            error = ex
        } finally {
            testStepRunResult.notify(null, error)
        }
    }

    override fun getTestStepClass(): Class<DefaultTestStep> {
        return DefaultTestStep::class.java
    }

    class Builder : BaseTestStepProcessor.Builder<DefaultTestStep>() {
        override fun build(): TestStepProcessor<DefaultTestStep> {
            return DefaultTestStepProcessor(testScenarioState ?: error("TestScenarioState must be provided"),
                    validator ?: error("Validator must be provided"),
                    testStepResultConverter)
        }
    }

}
