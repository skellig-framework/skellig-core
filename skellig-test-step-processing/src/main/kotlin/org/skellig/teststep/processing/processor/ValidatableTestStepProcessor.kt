package org.skellig.teststep.processing.processor

import org.skellig.task.TaskUtils.Companion.runTask
import org.skellig.teststep.processing.converter.TestStepResultConverter
import org.skellig.teststep.processing.exception.TestDataConversionException
import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.ValidationDetails
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.TestStepResultValidator

abstract class ValidatableTestStepProcessor<T : DefaultTestStep>(
        protected val testScenarioState: TestScenarioState,
        protected val validator: TestStepResultValidator,
        protected val testStepResultConverter: TestStepResultConverter?) : TestStepProcessor<T> {

    protected fun validate(testStep: DefaultTestStep) {
        validate(testStep, null)
    }

    protected fun validate(testStep: DefaultTestStep, actualResult: Any?) {
        testStep.validationDetails?.let { validationDetails ->
            validationDetails.testStepId?.let { testStepId ->
                getLatestResultOfTestStep(testStepId, testStep.delay, testStep.timeout)?.let {
                    validate(testStep.getId, validationDetails, it)
                } ?: run {
                    throw ValidationException(String.format("Result from test step with id '%s' was not found " +
                            "in Test Scenario State", testStepId))
                }
            } ?: validate(testStep.getId, validationDetails, actualResult)
        }
    }

    protected fun isValid(testStep: DefaultTestStep, actualResult: Any?) : Boolean =
        try {
            if (testStep.validationDetails != null) {
                validate(testStep.getId, testStep.validationDetails, actualResult)
                true
            } else actualResult != null
        } catch (ex: Exception) {
            false
        }

    protected fun validate(testStepId: String?, validationDetails: ValidationDetails, actualResult: Any?) {
        var newActualResult = actualResult
        try {
            validationDetails.convertTo?.let {
                newActualResult = testStepResultConverter?.convert(it, newActualResult)
                        ?: throw TestDataConversionException("No converter were declared for processor " + javaClass.name)
            }
            validator.validate(validationDetails.expectedResult, newActualResult)
        } catch (ex: ValidationException) {
            throw ValidationException(ex.message, testStepId)
        }
    }

    private fun getLatestResultOfTestStep(testStepId: String, delay: Int, timeout: Int): Any? {
        return runTask(
                { testScenarioState.get(testStepId + TestStepProcessor.RESULT_SAVE_SUFFIX) },
                { it != null },
                delay, timeout)
    }
}