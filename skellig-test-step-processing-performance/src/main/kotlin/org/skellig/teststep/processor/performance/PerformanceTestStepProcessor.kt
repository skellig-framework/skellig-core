package org.skellig.teststep.processor.performance

import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processor.performance.metrics.MetricsFactory
import org.skellig.teststep.processor.performance.model.LongRunResponse
import org.skellig.teststep.processor.performance.model.PerformanceTestStep
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Class responsible for processing performance test steps.
 *
 * The processing of [PerformanceTestStep] does the following steps:
 * 1) Runs test steps defined in [PerformanceTestStep.testStepsToRunBefore]
 * 2) Runs test steps defined in [PerformanceTestStep.run] in a separated task
 * 3) Runs test steps defined in [PerformanceTestStep.testStepsToRunAfter]
 * 4) Register time series for the whole run
 * 5) Notifies the subscribers with [LongRunResponse]
 *
 */
open class PerformanceTestStepProcessor protected constructor(
    private val testStepProcessor: TestStepProcessor<TestStep>,
    private val testStepRegistry: TestStepRegistry,
    private val metricsFactory: MetricsFactory
) : TestStepProcessor<PerformanceTestStep> {

    internal var isClosed = AtomicBoolean(false)

    override fun process(testStep: PerformanceTestStep): TestStepProcessor.TestStepRunResult {
        val response = LongRunResponse()

        if (!isClosed.get()) {
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

    private fun runTestSteps(
        testSteps: List<(testStepRegistry: TestStepRegistry) -> TestStep>,
        response: LongRunResponse
    ) {
        if (!isClosed.get()) {
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

    override fun getTestStepClass(): Class<PerformanceTestStep> = PerformanceTestStep::class.java


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

        fun build(): PerformanceTestStepProcessor =
            PerformanceTestStepProcessor(testStepProcessor!!, testStepRegistry!!, metricsFactory!!)
    }
}