package org.skellig.teststep.processor.performance.model

import org.skellig.teststep.processor.performance.metrics.TimeSeries

/**
 * Represents a response for test execution which runs for a long time.
 * This response is what [PerformanceTestStepProcessor][org.skellig.teststep.processor.performance.PerformanceTestStepProcessor] returns as a result of [PerformanceTestStep] processing
 */
class LongRunResponse {

    private val timeSeriesPerTestName = mutableMapOf<String, TimeSeries>()

    /**
     * Registers a time series for a specific test name.
     *
     * @param testName the name of the test
     * @param timeSeries the time series to register
     */
    fun registerTimeSeriesFor(testName: String, timeSeries: TimeSeries) {
        timeSeriesPerTestName[testName] = timeSeries
    }

    /**
     * Retrieves the time series data for each test name.
     *
     * @return the time series data per test name
     */
    fun getTimeSeries() = timeSeriesPerTestName
}