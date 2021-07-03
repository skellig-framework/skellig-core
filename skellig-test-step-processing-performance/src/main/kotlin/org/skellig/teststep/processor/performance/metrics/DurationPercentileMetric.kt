package org.skellig.teststep.processor.performance.metrics

interface DurationPercentileMetric : AutoCloseable, TimeSeries {

    fun createPercentileBucket() : DurationMetric

}