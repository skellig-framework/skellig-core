package org.skellig.teststep.processing.processor

import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.ValidationNode
import org.skellig.teststep.processing.state.TestScenarioState

/**
 * This is a basic processor for tests steps whose result can be validated.
 */
abstract class ValidatableTestStepProcessor<T : DefaultTestStep>(protected val testScenarioState: TestScenarioState) : TestStepProcessor<T> {

    /**
     * Validate any result (ex. from other test step).
     * Usually the result from another test step is taken by its `id` property using `get` function,
     * for example:
     *  [get(a).values().size > 0 = true]
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

}