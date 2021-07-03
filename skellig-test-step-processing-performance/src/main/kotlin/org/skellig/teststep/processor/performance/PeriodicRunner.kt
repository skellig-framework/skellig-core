package org.skellig.teststep.processor.performance

import kotlinx.coroutines.*
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processor.performance.metrics.MetricsFactory
import org.skellig.teststep.processor.performance.metrics.TimeSeries
import org.skellig.teststep.processor.performance.model.LongRunTestStep
import java.time.LocalDateTime
import kotlin.math.ceil
import kotlin.system.measureTimeMillis

internal class PeriodicRunner(private val testStep: LongRunTestStep,
                              private val testStepRegistry: TestStepRegistry,
                              metricsFactory: MetricsFactory) {

    companion object {
        private const val SEC_IN_NANOSEC = 1000000000
    }

    private val rps = testStep.rps
    private val finishTime = LocalDateTime.now().plusMinutes(testStep.timeToRun.toLong())
    private val delayTimeNs = (SEC_IN_NANOSEC / ceil(testStep.rps.toDouble())).toLong()
    private val durationPercentileMetric = metricsFactory.createDurationPercentileMetric(testStep.name)

    fun run(processingFunction: (TestStep) -> TestStepProcessor.TestStepRunResult) : TimeSeries {
        runBlocking {
            withContext(Dispatchers.Default) {
                try {
                    runTillTimeEnds(processingFunction)
                } finally {
                    durationPercentileMetric.close()
                }
            }
        }
        return durationPercentileMetric
    }

    private suspend fun runTillTimeEnds(processingFunction: (TestStep) -> TestStepProcessor.TestStepRunResult) {
        val time = measureTimeMillis {
            coroutineScope {
                do {
                    val durationMetric = durationPercentileMetric.createPercentileBucket()
                    repeat((1 until rps).count()) {
                        val startTime = System.nanoTime()
                        launch {
                            val totalElapsedTime = processAndGetTime(testStep, processingFunction)
                            durationMetric.recordTime(totalElapsedTime)
                        }
                        delayPrecise(startTime, delayTimeNs)
                    }
                    delay(1)
                } while (finishTime.isAfter(LocalDateTime.now()))
            }
        }
        println("total spent time = $time")
    }

    private fun processAndGetTime(testStep: LongRunTestStep,
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