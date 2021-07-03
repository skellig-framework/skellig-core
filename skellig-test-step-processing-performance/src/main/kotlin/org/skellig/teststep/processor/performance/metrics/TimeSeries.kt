package org.skellig.teststep.processor.performance.metrics

interface TimeSeries {

    fun consumeTimeSeriesRecords(recordConsumer: (String) -> Unit)
}