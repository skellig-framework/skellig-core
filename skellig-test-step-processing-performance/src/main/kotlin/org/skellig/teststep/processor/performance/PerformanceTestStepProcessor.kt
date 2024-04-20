package org.skellig.teststep.processor.performance

import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processor.performance.exception.PerformanceTestStepException
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
 * 3) Runs test steps defined in [PerformanceTestStep.testStepsToRunAfter] and return any errors happened in execution.
 * Each step will be awaited for completion before the processing is finished.
 * 4) Register time series for the whole run
 * 5) Notifies the subscribers with [LongRunResponse] and [PerformanceTestStepException] if any errors received from [PerformanceTestStep.testStepsToRunAfter].
 *
 */
open class PerformanceTestStepProcessor protected constructor(
    private val testStepProcessor: TestStepProcessor<TestStep>,
    private val testStepRegistry: TestStepRegistry,
    private val metricsFactory: MetricsFactory
) : TestStepProcessor<PerformanceTestStep> {

    internal var isClosed = AtomicBoolean(false)

    override fun process(testStep: PerformanceTestStep): TestStepProcessor.TestStepRunResult {
        val result = TestStepProcessor.TestStepRunResult(testStep)
        val response = LongRunResponse()
        var errorsAfter: Map<TestStep, RuntimeException>? = null

        if (!isClosed.get()) {
            runTestStepsBefore(testStep.testStepsToRunBefore, response)
            val periodicRunner = PeriodicRunner(this, testStep, testStepRegistry, metricsFactory)
            val timeSeries = periodicRunner.run { testStepProcessor.process(it) }

            errorsAfter = runTestStepsAfter(testStep.testStepsToRunAfter)

            response.registerTimeSeriesFor(testStep.name, timeSeries)
        }
        errorsAfter?.let { result.notify(response, PerformanceTestStepException(it)) } ?: result.notify(response, null)

        return result
    }

    private fun runTestStepsBefore(
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

    private fun runTestStepsAfter(testSteps: List<(testStepRegistry: TestStepRegistry) -> TestStep>)
            : Map<TestStep, RuntimeException> {
        return if (!isClosed.get()) {
            testSteps.mapNotNull {
                val testStep = it(testStepRegistry)
                val result = testStepProcessor.process(testStep)
                var error: RuntimeException? = null
                result.subscribe { _, _, ex -> error = ex }
                result.awaitResult()
                if (error != null) testStep to error!! else null
            }.toMap()
        } else emptyMap()
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