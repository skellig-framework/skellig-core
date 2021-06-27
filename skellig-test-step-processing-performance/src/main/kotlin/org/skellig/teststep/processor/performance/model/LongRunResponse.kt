package org.skellig.teststep.processor.performance.model

import org.skellig.teststep.processor.performance.model.timeseries.TimeSeries

class LongRunResponse {

    private val timeSeriesPerTestName = mutableMapOf<String, TimeSeries>()

    fun registerTimeSeriesFor(testName: String, timeSeries: TimeSeries) {
        timeSeriesPerTestName[testName] = timeSeries
    }

    fun getTimeSeries() = timeSeriesPerTestName
}