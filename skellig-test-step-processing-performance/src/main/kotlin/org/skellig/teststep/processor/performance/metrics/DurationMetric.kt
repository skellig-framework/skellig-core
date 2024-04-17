package org.skellig.teststep.processor.performance.metrics

/**
 * Interface for recording time measurements.
 */
interface DurationMetric {

    /**
     * Records the given time measurement.
     *
     * @param time the time measurement in milliseconds
     */
    fun recordTime(time: Long)

}