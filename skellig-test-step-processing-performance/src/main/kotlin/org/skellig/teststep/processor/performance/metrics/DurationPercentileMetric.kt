package org.skellig.teststep.processor.performance.metrics

/**
 * Interface for recording and analyzing duration percentiles.
 */
interface DurationPercentileMetric : AutoCloseable, TimeSeries {

    /**
     * Creates a new DurationMetric instance for recording time measurements.
     *
     * @return a DurationMetric instance
     */
    fun createPercentileBucket() : DurationMetric

}