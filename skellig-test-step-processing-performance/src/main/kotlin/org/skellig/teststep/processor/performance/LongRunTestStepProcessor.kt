package org.skellig.teststep.processor.performance

import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processor.performance.metrics.MetricsFactory
import org.skellig.teststep.processor.performance.model.LongRunResponse
import org.skellig.teststep.processor.performance.model.LongRunTestStep
import java.util.concurrent.atomic.AtomicBoolean

open class LongRunTestStepProcessor protected constructor(
    private val testStepProcessor: TestStepProcessor<TestStep>,
    private val testStepRegistry: TestStepRegistry,
    private val metricsFactory: MetricsFactory
) : TestStepProcessor<LongRunTestStep> {

    internal var isClosed = AtomicBoolean(false)

    override fun process(testStep: LongRunTestStep): TestStepProcessor.TestStepRunResult {
        val response = LongRunResponse()

        if(!isClosed.get()) {
            runTestSteps(testStep.testStepsToRunBefore, response)

            val periodicRunner = PeriodicRunner(this, testStep, testStepRegistry, metricsFactory)
            val timeSeries = periodicRunner.run { testStepProcessor.process(it) }

            runTestSteps(testStep.testStepsToRunAfter, response)

            response.registerTimeSeriesFor(testStep.name, timeSeries)
        }

        val result = TestStepProcessor.TestStepRunResult(testStep)
        result.notify(response, null)
        return result
    }

    private fun runTestSteps(testSteps: List<(testStepRegistry: TestStepRegistry) -> TestStep>,
                             response: LongRunResponse) {
        if(!isClosed.get()) {
            testSteps.forEach {
                val testStep = it(testStepRegistry)
                val timeSeries = metricsFactory.createMessageReceptionMetric(testStep.name)
                response.registerTimeSeriesFor(testStep.name, timeSeries)
                testStepProcessor.process(testStep)
                    .subscribe { _, _, ex ->
                        if (ex == null) timeSeries.registerMessageReception()
                        else timeSeries.registerMessageFailed()
                    }
            }
        }
    }

    override fun close() {
        isClosed.set(true)
    }

    override fun getTestStepClass(): Class<LongRunTestStep> = LongRunTestStep::class.java


    class Builder {

        private var testStepProcessor: TestStepProcessor<TestStep>? = null
        private var testStepRegistry: TestStepRegistry? = null
        private var metricsFactory: MetricsFactory? = null

        fun testStepProcessor(testStepProcessor: TestStepProcessor<TestStep>) = apply {
            this.testStepProcessor = testStepProcessor
        }

        fun testStepRegistry(testStepRegistry: TestStepRegistry) = apply {
            this.testStepRegistry = testStepRegistry
        }

        fun metricsFactory(metricsFactory: MetricsFactory?) = apply {
            this.metricsFactory = metricsFactory
        }

        fun build(): LongRunTestStepProcessor =
            LongRunTestStepProcessor(testStepProcessor!!, testStepRegistry!!, metricsFactory!!)
    }
}