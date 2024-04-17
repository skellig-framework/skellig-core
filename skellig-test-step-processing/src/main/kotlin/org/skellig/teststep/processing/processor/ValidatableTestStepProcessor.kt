package org.skellig.teststep.processing.processor

import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.ValidationNode
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.util.error
import org.skellig.teststep.processing.util.info
import org.skellig.teststep.processing.util.logger

/**
 * This is a basic processor for tests steps whose result can be validated.
 */
abstract class ValidatableTestStepProcessor<T : DefaultTestStep>(protected val testScenarioState: TestScenarioState) : TestStepProcessor<T> {

    private val log = logger<ValidatableTestStepProcessor<*>>()

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
    protected open fun validate(testStep: T, actualResult: Any?) {
        testStep.validationDetails?.let { expectedResult ->
            validate(testStep, expectedResult, actualResult)
        }
    }

    /**
     * Checks whether the result of processing of the test step matches with the expected
     * result, provided in its validation details.
     */
    protected open fun isValid(testStep: T, actualResult: Any?): Boolean =
        try {
            if (testStep.validationDetails != null) {
                validate(testStep, testStep.validationDetails, actualResult)
                true
            } else actualResult != null
        } catch (ex: Exception) {
            log.error(testStep, "Validation failed: ${ex.message}")
            false
        }

    protected open fun validate(testStep: T, expectedResult: ValidationNode, actualResult: Any?) {
        try {
            log.info(testStep, "Start to validate the result of processed test step '${testStep.name}'")
            expectedResult.validate(actualResult)
        } catch (ex: ValidationException) {
            throw ValidationException(ex.message)
        }
    }

}