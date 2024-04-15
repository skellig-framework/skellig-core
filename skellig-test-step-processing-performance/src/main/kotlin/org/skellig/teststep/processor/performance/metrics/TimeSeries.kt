package org.skellig.teststep.processor.performance.metrics

/**
 * Represents a time series of records.
 */
interface TimeSeries {

    /**
     * Consumes the time series records by invoking the provided recordConsumer function.
     *
     * @param recordConsumer the function that will consume the time series records
     */
    fun consumeTimeSeriesRecords(recordConsumer: (String) -> Unit)
}