package org.skellig.teststep.processing.processor

import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.processor.TestStepProcessor.TestStepRunResult
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.util.info
import org.skellig.teststep.processing.util.logTestStepResult
import org.skellig.teststep.processing.util.logger

/**
 * Processes a default test step by running validation of a result from another test step.
 */
internal class DefaultTestStepProcessor private constructor(testScenarioState: TestScenarioState) : ValidatableTestStepProcessor<DefaultTestStep>(testScenarioState) {

    private val log = logger<DefaultTestStepProcessor>()

    override fun process(testStep: DefaultTestStep): TestStepRunResult {
        val testStepRunResult = TestStepRunResult(testStep)
        testScenarioState.set(testStep.getId, testStep)
        log.info(testStep, "Start to process task of test '${testStep.name}' by running validation only")
        validate(testStep, testStepRunResult)

        return testStepRunResult
    }

    private fun validate(testStep: DefaultTestStep, testStepRunResult: TestStepRunResult) {
        var error: RuntimeException? = null
        try {
            validate(testStep)
        } catch (ex: ValidationException) {
            error = ex
        } finally {
            log.logTestStepResult(testStep, "none", error)
            testStepRunResult.notify(null, error)
        }
    }

    /**
     * Validate any result (ex. from other test step).
     * Usually the result from another test step is taken by its `id` property using `get` function,
     * for example:
     *  [get(testA_result).values().size > 0 = true]
     */
    @Throws(ValidationException::class)
    private fun validate(testStep: DefaultTestStep) {
        testStep.validationDetails?.let { validationDetails ->
            validate(testStep, validationDetails, null)
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
