package org.skellig.teststep.processing.processor

import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.processor.TestStepProcessor.TestStepRunResult
import java.io.Closeable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Responsible for processing a test step (ex. a .sts-file or java/kotlin-class)
 * and returning [result][TestStepRunResult].
 *
 * Each processor is bound to a specific [TestStep] which has essential properties
 * required for processing and getting a result from it.
 *
 * Some processors can have resources which need to close thus it implements [Closeable]
 * without any actions by default. Usually this method is used
 * when all tests are finished or one of them failed.
 *
 * @see TestStep
 * @see process
 */
interface TestStepProcessor<T : TestStep> : Closeable {

    companion object {
        const val RESULT_SAVE_SUFFIX = "_result"
    }

    /**
     * Takes a test step, processes it and returns a result.
     * The [TestStepRunResult] captures thr original test step, a response from processing of the test step and
     * an [error][RuntimeException].
     *
     * Note, that if any exception occurs when processing a test step, it will be wrapped to
     * [TestStepProcessingException] or in some cases - [RuntimeException], and passed further to the response when
     * calls [TestStepRunResult.notify]. The exception won't be thrown in this method.
     *
     * @see TestStepRunResult
     */
    fun process(testStep: T): TestStepRunResult

    /**
     * A class of the test step. This is required for a registry
     * in order to identify which processor to assign for a provided test step.
     */
    fun getTestStepClass(): Class<*>

    /**
     * Close resources if needed for the processor.
     * By default, this method does nothing.
     */
    override fun close() {}

    /**
     * The async or sync result of a processed test step.
     *
     */
    open class TestStepRunResult(private val testStep: TestStep?) {

        private var consumer: ((TestStep?, Any?, RuntimeException?) -> Unit)? = null
        private var response: Any? = null
        private var error: RuntimeException? = null
        private val countDownLatch: CountDownLatch = CountDownLatch(1)

        /**
         * Subscribe for a result of a processed test step,
         * whether it's a successful result or an exception.
         *
         * If it's an async or sync-test step, then it notifies about the result
         * when it's ready after the processing is finished.
         */
        fun subscribe(consumer: (TestStep?, Any?, RuntimeException?) -> Unit) {
            this.consumer = consumer
            if (isFinished()) {
                notify(response, error)
            }
        }

        /**
         * Notify the test step about the result of processing.
         */
        fun notify(response: Any?, error: RuntimeException?) {
            this.response = response
            this.error = error
            countDownLatch.countDown()
            consumer?.let {
                it(testStep, response, error)
            }
        }

        /**
         * Wait for result of the processed test step.
         * If the test step is already finished, then returns immediately with no actions taken.
         *
         * If the test step is not finished yet, then waits for a result with the provided timeout. If result has not been
         * received within the timeout, then throws [TestStepProcessingException].
         * If result has been received within the timeout but was notified about an error, then throws [TestStepProcessingException]
         */
        @Throws(TestStepProcessingException::class)
        fun awaitResult() {
            if (testStep != null && !isFinished()) {
                try {
                    if (!countDownLatch.await(getTimeout(), TimeUnit.MILLISECONDS)) {
                        error = TestStepProcessingException("Failed to received the final result of test step '${testStep.name}' within ${getTimeout()} ms")
                    } else if (error != null) {
                        error = TestStepProcessingException("Failed to process test step '${testStep.name}'", error)
                    }
                } catch (ex: InterruptedException) {
                    error =
                        TestStepProcessingException(
                            String.format(
                                "Failed to get response from test step '%s' within %d seconds",
                                testStep.name, getTimeout()
                            ), ex
                        )
                    notify(null, error)
                }
                if (error != null) {
                    throw error as RuntimeException
                }
            }
        }

        private fun isFinished() = countDownLatch.count == 0L

        /**
         * Get timeout in milliseconds for waiting a response.
         */
        protected open fun getTimeout(): Long = 0

    }
}