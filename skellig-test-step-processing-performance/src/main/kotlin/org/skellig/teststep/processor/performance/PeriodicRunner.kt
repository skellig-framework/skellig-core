package org.skellig.teststep.processor.performance

import kotlinx.coroutines.*
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processor.performance.model.LongRunTestStep
import org.skellig.teststep.processor.performance.model.timeseries.PercentileTimeSeries
import org.skellig.teststep.processor.performance.model.timeseries.PercentileTimeSeriesItem
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import kotlin.concurrent.fixedRateTimer
import kotlin.math.ceil
import kotlin.system.measureTimeMillis

internal class PeriodicRunner(private val testStep: LongRunTestStep, private val testStepRegistry: TestStepRegistry) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(PeriodicRunner::class.java)
        private const val SEC_IN_NANOSEC = 1000000000
        private const val COMPRESSION_TIME_PERIOD = 120 * 1000L
        private const val TIME_SERIES_COMPRESSION_PERCENTAGE = 80
    }

    private val rps = testStep.rps
    private val finishTime = LocalDateTime.now().plusMinutes(testStep.timeToRun.toLong())
    private val delayTimeNs = (SEC_IN_NANOSEC / ceil(testStep.rps.toDouble())).toLong()
    private val timeSeries = PercentileTimeSeries(testStep.name)
    private val timer = fixedRateTimer("timer", false, COMPRESSION_TIME_PERIOD, COMPRESSION_TIME_PERIOD) {
        measureTimeMillis {
            timeSeries.compress(TIME_SERIES_COMPRESSION_PERCENTAGE)
        }.also { LOGGER.debug("Successfully compressed time series data in $it ms") }
    }

    fun run(processingFunction: (TestStep) -> TestStepProcessor.TestStepRunResult) : PercentileTimeSeries {
        runBlocking {
            withContext(Dispatchers.Default) {
                try {
                    runTillTimeEnds(processingFunction)
                } finally {
                    timer.cancel()
                    timeSeries.compress()
                }
            }
        }
        return timeSeries
    }

    private suspend fun runTillTimeEnds(processingFunction: (TestStep) -> TestStepProcessor.TestStepRunResult) {
        val time = measureTimeMillis {
            coroutineScope {
                do {
                    val timeSeriesItem = PercentileTimeSeriesItem()
                    timeSeries.add(timeSeriesItem)
                    repeat((1 until rps).count()) {
                        val startTime = System.nanoTime()
                        launch {
                            val totalElapsedTime = processAndGetTime(testStep, processingFunction)
                            timeSeriesItem.recordTime(totalElapsedTime)
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