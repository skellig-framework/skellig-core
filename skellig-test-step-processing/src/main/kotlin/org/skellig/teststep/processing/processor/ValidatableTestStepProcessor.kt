package org.skellig.teststep.processing.processor

import org.skellig.task.TaskUtils.Companion.runTask
import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.ValidationDetails
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.TestStepResultValidator
import org.skellig.teststep.processing.validation.ValidationNode

/**
 * This is a basic processor for tests steps whose result can be validated.
 */
abstract class ValidatableTestStepProcessor<T : DefaultTestStep>(
    protected val testScenarioState: TestScenarioState,
    protected val validator: TestStepResultValidator,
) : TestStepProcessor<T> {

    /**
     * Validated test step from another test.
     * Usually the result from another test step is taken by its `id` property,
     * and indicated in `fromTest` property inside `validation` section of the test step.
     *
     * If no result from another test is found, then it throws `ValidationException`
     */
    @Throws(ValidationException::class)
    protected fun validate(testStep: DefaultTestStep) {
        testStep.validationDetails?.let { validationDetails ->
            validate(testStep.getId, validationDetails, null)
        }
    }

    /**
     * Validate test step actual result with expected one.
     * The expected result is part of `validation` section of the test step.
     *
     * If no validation details provided then it does nothing.
     *
     * If validation fails, then it throws `ValidationException`
     * @see ValidationException
     */
    @Throws(ValidationException::class)
    protected fun validate(testStep: DefaultTestStep, actualResult: Any?) {
        testStep.validationDetails?.let { validationDetails ->
            validate(testStep.getId, validationDetails, actualResult)
        }
    }

    /**
     * Checks whether the result of processing of the test step matches with the expected
     * result, provided in its validation details.
     */
    protected fun isValid(testStep: DefaultTestStep, actualResult: Any?): Boolean =
        try {
            if (testStep.validationDetails != null) {
                validate(testStep.getId, testStep.validationDetails, actualResult)
                true
            } else actualResult != null
        } catch (ex: Exception) {
            false
        }

    protected fun validate(testStepId: String?, validationDetails: ValidationNode, actualResult: Any?) {
        try {
            validationDetails.validate(actualResult)
        } catch (ex: ValidationException) {
            throw ValidationException(ex.message, testStepId)
        }
    }

    /**
     * Gets the result from a test step by its id.
     * Delay and timeout is needed in case if the state hasn't yet received the result
     * at the time this method is called.
     */
    private fun getLatestResultOfTestStep(testStepId: String, delay: Int, timeout: Int): Any? {
        return runTask(
            { testScenarioState.get(testStepId + TestStepProcessor.RESULT_SAVE_SUFFIX) },
            { it != null },
            delay, timeout
        )
    }
}