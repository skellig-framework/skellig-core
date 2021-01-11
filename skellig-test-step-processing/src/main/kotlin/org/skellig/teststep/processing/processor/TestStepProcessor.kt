package org.skellig.teststep.processing.processor

import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.model.TestStep
import java.io.Closeable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

interface TestStepProcessor<T : TestStep> : Closeable {

    fun process(testStep: T): TestStepRunResult

    fun getTestStepClass(): Class<*>

    override fun close() {}

    open class TestStepRunResult(private val testStep: TestStep?) {

        private var consumer: ((TestStep?, Any?, RuntimeException?) -> Unit)? = null
        private var response: Any? = null
        private var error: RuntimeException? = null
        private val countDownLatch: CountDownLatch = CountDownLatch(1)

        fun subscribe(consumer: (TestStep?, Any?, RuntimeException?) -> Unit) {
            this.consumer = consumer
            if (countDownLatch.count == 0L) {
                notify(response, error)
            }
        }

        fun notify(response: Any?, error: RuntimeException?) {
            this.response = response
            this.error = error
            countDownLatch.countDown()
            consumer?.let {
                it(testStep, response, error)
            }
        }

        @Throws(TestStepProcessingException::class)
        fun awaitResult() {
            if (testStep != null) {
                try {
                    countDownLatch.await(getTimeout(), TimeUnit.SECONDS)
                    if (error != null) {
                        error = TestStepProcessingException(String.format("Failed to process test step '%s'", testStep.name), error)
                    }
                } catch (ex: InterruptedException) {
                    error = TestStepProcessingException(String.format("Failed to get response from test step '%s' within %d seconds",
                            testStep.name, getTimeout()), ex)
                    notify(null, error)
                }
                if (error != null) {
                    throw error as RuntimeException
                }
            }
        }

        protected open fun getTimeout() : Long = 0

    }
}