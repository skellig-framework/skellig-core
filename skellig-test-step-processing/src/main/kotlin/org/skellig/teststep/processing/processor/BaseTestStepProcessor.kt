package org.skellig.teststep.processing.processor

import kotlinx.coroutines.*
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.processor.TestStepProcessor.TestStepRunResult
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.util.debug
import org.skellig.teststep.processing.util.logTestStepResult
import org.skellig.teststep.processing.util.logger
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

private const val DEFAULT_GRACEFUL_SHUTDOWN_WAIT_SEC = 30L

/**
 * Base processor for [DefaultTestStep].
 * It can handle `sync` and `async` execution, stores the data of test step in the state,
 * as well as its result after execution. After the result is received, it's validated
 * based on the validation details in the test step.
 *
 * @param testScenarioState The [TestScenarioState] object which stores data during a run of test scenario and accessible
 * in all test step processors. The data can be saved in the state through the details provided in [DefaultTestStep.scenarioStateUpdaters]
 */
abstract class BaseTestStepProcessor<T : DefaultTestStep>(testScenarioState: TestScenarioState) : ValidatableTestStepProcessor<T>(testScenarioState) {

    private val log = logger<BaseTestStepProcessor<*>>()
    private val asyncTaskGroupExecutor = AsyncTaskGroupExecutor()
    private val asyncTaskExecutor = Executors.newCachedThreadPool()

    override fun process(testStep: T): TestStepRunResult {
        val testStepRunResult = DefaultTestStepRunResult(testStep)
        testScenarioState.set(testStep.getId, testStep)

        when (testStep.execution) {
            TestStepExecutionType.ASYNC -> asyncTaskExecutor.execute {
                log.debug(testStep) { "Run the test step asynchronously" }
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

    override fun close() {
        var isGracefullyShutdown = false
        try {
            asyncTaskExecutor.shutdown()
            isGracefullyShutdown = asyncTaskExecutor.awaitTermination(DEFAULT_GRACEFUL_SHUTDOWN_WAIT_SEC, TimeUnit.SECONDS)
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()
        } finally {
            if (!isGracefullyShutdown) asyncTaskExecutor.shutdownNow()
        }
    }

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
            log.logTestStepResult(testStep, result, error)
            testStepRunResult.notify(result, error)
        }
    }

    protected fun <R> runTasksAsyncAndWait(tasks: Map<*, () -> R>, testStep: T): Map<Any?, R?> {
        return asyncTaskGroupExecutor.runTasksAsyncAndWait(tasks, { isValid(testStep, it) }, testStep.delay, testStep.attempts, testStep.timeout)
    }

    class AsyncTaskGroupExecutor {

        private val log = logger<AsyncTaskGroupExecutor>()

        fun <R> runTasksAsyncAndWait(
            tasks: Map<*, () -> R>,
            stopCondition: (Map<*, R?>) -> Boolean = { true },
            delay: Int = 0, attempts: Int = 0, timeout: Int = 0
        ): Map<Any?, R?> {
            return runBlocking {
                withContext(Dispatchers.IO) {
                    forEachAsync(tasks, stopCondition, delay, attempts, timeout, mapOf<Any, R?>())
                }
            }
        }

        private suspend fun <R> forEachAsync(
            tasks: Map<*, () -> R>,
            stopCondition: (Map<*, R?>) -> Boolean = { true },
            delay: Int = 0, attempts: Int = 0, timeout: Int,
            previousResults: Map<*, R?>
        ): Map<Any?, R?> {
            return coroutineScope {
                val newAttempt = attempts - 1
                val futures = tasks.map {
                    it.key to async { it.value.invoke() }
                }.toMap()

                val finalResult = futures.map {
                    // if result is null then try to get non-null value from previous result if exists
                    it.key to (waitAndGetResult(it.value, timeout) ?: previousResults[it.key])
                }.toMap()

                if (newAttempt <= 0 || stopCondition(finalResult)) {
                    finalResult
                } else {
                    delay(delay.toLong())
                    forEachAsync(tasks, stopCondition, delay, newAttempt, timeout, finalResult)
                }
            }
        }

        private suspend fun <R> waitAndGetResult(taskResult: Deferred<R?>, timeout: Int) =
            try {
                if (timeout > 0) {
                    withTimeout(timeout.toLong()) {
                        taskResult.await()
                    }
                } else taskResult.await()
            } catch (ex: Exception) {
                taskResult.cancel(ex.message ?: "", ex)
                when (ex) {
                    is InterruptedException, is TimeoutException, is TimeoutCancellationException -> {
                        log.error("Failed to wait for the response of the task as it exceeded the timeout of $timeout ms. Return null instead")
                        null
                    }
                    // in case if exception comes from the task then throw it
                    else -> ex.cause?.let { throw it } ?: throw ex
                }
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