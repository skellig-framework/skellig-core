package org.skellig.teststep.processor.performance.metrics

/**
 * Represents a factory for creating metrics.
 */
interface MetricsFactory {

    /**
     * Creates a [DurationPercentileMetric] with the given name.
     * The [DurationPercentileMetric] is used for recording and analyzing duration percentiles.
     *
     * @param name the name of the [DurationPercentileMetric]
     * @return a [DurationPercentileMetric] instance
     */
    fun createDurationPercentileMetric(name : String) : DurationPercentileMetric

    /**
     * Creates a [MessageReceptionMetric] with the given name.
     * The [MessageReceptionMetric] is used for measuring and recording message reception.
     *
     * @param name the name of the [MessageReceptionMetric]
     * @return a [MessageReceptionMetric] instance
     */
    fun createMessageReceptionMetric(name : String) : MessageReceptionMetric
}