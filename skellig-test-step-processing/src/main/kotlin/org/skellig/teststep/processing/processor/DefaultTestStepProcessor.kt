package org.skellig.teststep.processing.processor

import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.processor.TestStepProcessor.TestStepRunResult
import org.skellig.teststep.processing.state.TestScenarioState

/**
 * Processes a default test step by running validation of a result from another test step.
 */
internal class DefaultTestStepProcessor private constructor(testScenarioState: TestScenarioState) : ValidatableTestStepProcessor<DefaultTestStep>(testScenarioState) {

    override fun process(testStep: DefaultTestStep): TestStepRunResult {
        val testStepRunResult = TestStepRunResult(testStep)
        testScenarioState.set(testStep.getId, testStep)
        validate(testStep, testStepRunResult)

        return testStepRunResult
    }

    private fun validate(testStep: DefaultTestStep, testStepRunResult: TestStepRunResult) {
        var error: RuntimeException? = null
        try {
            super.validate(testStep)
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
            return DefaultTestStepProcessor(testScenarioState ?: error("TestScenarioState must be provided"))
        }
    }

}
