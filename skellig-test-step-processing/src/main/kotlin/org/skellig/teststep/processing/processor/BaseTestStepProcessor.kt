package org.skellig.teststep.processing.processor

import org.skellig.task.async.AsyncTaskUtils.Companion.runTaskAsync
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.processor.TestStepProcessor.TestStepRunResult
import org.skellig.teststep.processing.state.TestScenarioState
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Base processor for `DefaultTestStep`.
 * It can handle `sync` and `async` execution, stores the data of test step in the state,
 * as well as its result after execution. After the result is received, it's validated
 * based on the validation details in the test step.
 */
abstract class BaseTestStepProcessor<T : DefaultTestStep>(testScenarioState: TestScenarioState) : ValidatableTestStepProcessor<T>(testScenarioState) {

    private val log: Logger = LoggerFactory.getLogger(BaseTestStepProcessor::class.java)

    override fun process(testStep: T): TestStepRunResult {
        val testStepRunResult = DefaultTestStepRunResult(testStep)
        testScenarioState.set(testStep.getId, testStep)

        when (testStep.execution) {
            TestStepExecutionType.ASYNC -> runTaskAsync {
                log.debug("[${testStep.hashCode()}]: Run the test step asynchronously")
                processAndValidate(testStep, testStepRunResult)
            }

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
            testStep.scenarioStateUpdaters?.let { updaters ->
                updaters.forEach { it.update(result, testScenarioState) }
            }
            validate(testStep, result)
        } catch (ex: Throwable) {
            error = when (ex) {
                is ValidationException, is TestStepProcessingException -> ex as RuntimeException
                else -> TestStepProcessingException(ex.message, ex)
            }
        } finally {
            log.debug("[${testStep.hashCode()}]: Notify the subscribers with result of test processing")
            testStepRunResult.notify(result, error)
        }
    }


    abstract class Builder<T : TestStep> {
        protected var testScenarioState: TestScenarioState? = null

        /**
         * The scenario state is needed to store the data of test step,
         * as well as its result after processing.
         */
        fun withTestScenarioState(testScenarioState: TestScenarioState?) =
            apply { this.testScenarioState = testScenarioState }

        abstract fun build(): TestStepProcessor<T>
    }

    class DefaultTestStepRunResult(private val testStep: DefaultTestStep?) : TestStepRunResult(testStep) {

        override fun getTimeout(): Long {
            val attempts = testStep?.attempts ?: 1
            return ((if (attempts == 0) 1 else attempts) * (testStep?.timeout ?: 0)).toLong()
        }
    }
}