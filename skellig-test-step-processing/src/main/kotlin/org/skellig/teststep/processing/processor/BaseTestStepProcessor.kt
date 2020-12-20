package org.skellig.teststep.processing.processor

import org.skellig.task.async.AsyncTaskUtils.Companion.runTaskAsync
import org.skellig.teststep.processing.converter.TestStepResultConverter
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.processor.TestStepProcessor.TestStepRunResult
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.TestStepResultValidator

abstract class BaseTestStepProcessor<T : TestStep>(
        testScenarioState: TestScenarioState,
        validator: TestStepResultValidator,
        testStepResultConverter: TestStepResultConverter?)
    : ValidatableTestStepProcessor<T>(testScenarioState, validator, testStepResultConverter) {

    override fun process(testStep: T): TestStepRunResult {
        val testStepRunResult = TestStepRunResult(testStep)
        testScenarioState.set(testStep.id, testStep)

        when (testStep.execution) {
            TestStepExecutionType.ASYNC -> runTaskAsync { processAndValidate(testStep, testStepRunResult) }
            else -> processAndValidate(testStep, testStepRunResult)
        }

        return testStepRunResult
    }

    protected abstract fun processTestStep(testStep: T): Any?

    private fun processAndValidate(testStep: T, testStepRunResult: TestStepRunResult) {
        var result: Any? = null
        var error: RuntimeException? = null
        try {
            result = processTestStep(testStep)
            testScenarioState.set(testStep.id + RESULT_SAVE_SUFFIX, result)
            validate(testStep, result)
        } catch (ex: ValidationException) {
            error = ex
        } catch (ex: TestStepProcessingException) {
            error = ex
        } catch (ex: Throwable) {
            error = TestStepProcessingException(ex.message, ex)
        } finally {
            testStepRunResult.notify(result, error)
        }
    }


    abstract class Builder<T : TestStep> {
        protected var testScenarioState: TestScenarioState? = null
        protected var validator: TestStepResultValidator? = null
        protected var testStepResultConverter: TestStepResultConverter? = null

        fun withTestScenarioState(testScenarioState: TestScenarioState?) =
                apply { this.testScenarioState = testScenarioState }

        fun withValidator(validator: TestStepResultValidator?) =
                apply { this.validator = validator }

        fun withTestStepResultConverter(testStepResultConverter: TestStepResultConverter?) =
                apply { this.testStepResultConverter = testStepResultConverter }

        abstract fun build(): TestStepProcessor<T>
    }
}