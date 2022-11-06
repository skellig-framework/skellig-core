package org.skellig.teststep.processing.processor

import org.skellig.task.async.AsyncTaskUtils.Companion.runTaskAsync
import org.skellig.teststep.processing.converter.TestStepResultConverter
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.processor.TestStepProcessor.TestStepRunResult
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.TestStepResultValidator

/**
 * Base processor for `DefaultTestStep`.
 * It can handle `sync` and `async` execution, stores the data of test step in the state,
 * as well as its result after execution. After the result is received, it's validated
 * based on the validation details in the test step.
 */
abstract class BaseTestStepProcessor<T : DefaultTestStep>(
    testScenarioState: TestScenarioState,
    validator: TestStepResultValidator,
    testStepResultConverter: TestStepResultConverter?
) : ValidatableTestStepProcessor<T>(testScenarioState, validator, testStepResultConverter) {

    override fun process(testStep: T): TestStepRunResult {
        val testStepRunResult = DefaultTestStepRunResult(testStep)
        testScenarioState.set(testStep.getId, testStep)

        when (testStep.execution) {
            TestStepExecutionType.ASYNC -> runTaskAsync { processAndValidate(testStep, testStepRunResult) }
            else -> processAndValidate(testStep, testStepRunResult)
        }

        return testStepRunResult
    }

    /**
     * Main method for processing test step.
     * If the test step execution type is `async`, then it will run
     * in a separated thread.
     *
     * When result is received, it will be validated if required.
     *
     * The method catches all exceptions and uses them as an error when
     * notifying about the processing outcome.
     *
     * @see TestStepRunResult.notify
     */
    protected abstract fun processTestStep(testStep: T): Any?

    private fun processAndValidate(testStep: T, testStepRunResult: TestStepRunResult) {
        var result: Any? = null
        var error: RuntimeException? = null
        try {
            result = processTestStep(testStep)
            testScenarioState.set(testStep.getId + TestStepProcessor.RESULT_SAVE_SUFFIX, result)
            validate(testStep, result)
        } catch (ex: Throwable) {
            error = when (ex) {
                is ValidationException, is TestStepProcessingException -> ex as RuntimeException
                else -> TestStepProcessingException(ex.message, ex)
            }
        } finally {
            testStepRunResult.notify(result, error)
        }
    }


    abstract class Builder<T : TestStep> {
        protected var testScenarioState: TestScenarioState? = null
        protected var validator: TestStepResultValidator? = null
        protected var testStepResultConverter: TestStepResultConverter? = null

        /**
         * The scenario state is needed to store the data of test step,
         * as well as its result after processing.
         */
        fun withTestScenarioState(testScenarioState: TestScenarioState?) =
            apply { this.testScenarioState = testScenarioState }

        /**
         * The validator is required for validating the result after processing
         * of a test step.
         */
        fun withValidator(validator: TestStepResultValidator?) =
            apply { this.validator = validator }

        /**
         * The result converter is needed for converting an actual result from processing
         * of a test step before validation.
         */
        fun withTestStepResultConverter(testStepResultConverter: TestStepResultConverter?) =
            apply { this.testStepResultConverter = testStepResultConverter }

        abstract fun build(): TestStepProcessor<T>
    }

    class DefaultTestStepRunResult(private val testStep: DefaultTestStep?) : TestStepProcessor.TestStepRunResult(testStep) {

        override fun getTimeout(): Long {
            val attempts = testStep?.attempts ?: 1
            return ((if (attempts == 0) 1 else attempts) * (testStep?.timeout ?: 0)).toLong()
        }
    }
}