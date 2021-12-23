package org.skellig.teststep.processor.performance

import kotlinx.coroutines.*
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processor.performance.metrics.MetricsFactory
import org.skellig.teststep.processor.performance.metrics.TimeSeries
import org.skellig.teststep.processor.performance.model.PerformanceTestStep
import java.time.LocalDateTime
import kotlin.math.ceil

internal class PeriodicRunner(private val owner: PerformanceTestStepProcessor,
                              private val testStep: PerformanceTestStep,
                              private val testStepRegistry: TestStepRegistry,
                              metricsFactory: MetricsFactory) {

    companion object {
        private const val SEC_IN_NANOSEC = 1000000000
    }

    private val rps = testStep.rps
    private val finishTime = LocalDateTime.now().plusSeconds(testStep.timeToRun.toSecondOfDay().toLong())
    private val delayTimeNs = (SEC_IN_NANOSEC / ceil(testStep.rps.toDouble())).toLong()
    private val durationPercentileMetric = metricsFactory.createDurationPercentileMetric(testStep.name)

    fun run(processingFunction: (TestStep) -> TestStepProcessor.TestStepRunResult): TimeSeries {
        runBlocking {
            withContext(Dispatchers.Default) {
                durationPercentileMetric.use {
                    runTillTimeEnds(processingFunction)
                }
            }
        }
        return durationPercentileMetric
    }

    private suspend fun runTillTimeEnds(processingFunction: (TestStep) -> TestStepProcessor.TestStepRunResult) {
        coroutineScope {
            do {
                val durationMetric = durationPercentileMetric.createPercentileBucket()
                repeat((0 until rps).count()) {
                    val startTime = System.nanoTime()
                    launch {
                        val totalElapsedTime = processAndGetTime(testStep, processingFunction)
                        durationMetric.recordTime(totalElapsedTime)
                    }
                    delayPrecise(startTime, delayTimeNs)
                }
                delay(1)
            } while (!owner.isClosed.get() && finishTime.isAfter(LocalDateTime.now()))
        }
    }

    private fun processAndGetTime(testStep: PerformanceTestStep,
                                  processingFunction: (TestStep) -> TestStepProcessor.TestStepRunResult): Long {
        var end = 0L
        var requestStatus = true

        testStep.testStepsToRun
            .forEach {
                val initializedTestStep = it(testStepRegistry)

                val start = System.currentTimeMillis()
                processingFunction(initializedTestStep)
                    .subscribe { _, _, ex ->
                        end += System.currentTimeMillis() - start
                        requestStatus = requestStatus && ex != null
                    }
            }
        return end
    }

    private fun delayPrecise(startTime: Long, delayTimeNs: Long) {
        while (System.nanoTime() - startTime <= delayTimeNs) {
        }
    }
}