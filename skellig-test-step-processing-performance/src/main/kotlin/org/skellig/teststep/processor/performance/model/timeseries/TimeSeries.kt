package org.skellig.teststep.processor.performance.model.timeseries

interface TimeSeries {

    fun consumeTimeSeriesRecords(recordConsumer: (String) -> Unit)
}