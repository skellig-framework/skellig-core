package org.skellig.teststep.processing.processor

import org.skellig.task.TaskUtils.Companion.runTask
import org.skellig.teststep.processing.converter.TestStepResultConverter
import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.ValidationDetails
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.TestStepResultValidator

abstract class ValidatableTestStepProcessor<T : TestStep>(
        protected val testScenarioState: TestScenarioState,
        protected val validator: TestStepResultValidator,
        protected val testStepResultConverter: TestStepResultConverter?) : TestStepProcessor<T> {

    companion object {
        const val RESULT_SAVE_SUFFIX = ".result"
    }

    protected fun validate(testStep: TestStep) {
        validate(testStep, null)
    }

    protected fun validate(testStep: TestStep, actualResult: Any?) {
        testStep.validationDetails?.let { validationDetails ->
            validationDetails.testStepId?.let { testStepId ->
                getLatestResultOfTestStep(testStepId, testStep.delay, testStep.timeout)?.let {
                    validate(testStep.id, validationDetails, it)
                } ?: {
                    throw ValidationException(String.format("Result from test step with id '%s' was not found " +
                            "in Test Scenario State", testStepId))
                }
            } ?: validate(testStep.id, validationDetails, actualResult)
        }
    }

    private fun validate(testStepId: String?, validationDetails: ValidationDetails, actualResult: Any?) {
        var newActualResult = actualResult
        try {
            validationDetails.convertTo?.let {
                newActualResult = testStepResultConverter!!.convert(it, newActualResult)
            }
            validator.validate(validationDetails.expectedResult, newActualResult)
        } catch (ex: ValidationException) {
            throw ValidationException(ex.message, testStepId)
        }
    }

    private fun getLatestResultOfTestStep(testStepId: String, delay: Int, timeout: Int): Any? {
        return runTask(
                { testScenarioState.get(testStepId + RESULT_SAVE_SUFFIX) },
                { obj: Any? -> obj != null },
                delay, timeout)
    }
}