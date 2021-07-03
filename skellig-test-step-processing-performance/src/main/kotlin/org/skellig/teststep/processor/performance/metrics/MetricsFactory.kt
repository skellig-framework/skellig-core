package org.skellig.teststep.processor.performance.metrics

interface MetricsFactory {

    fun createDurationPercentileMetric(name : String) : DurationPercentileMetric

    fun createMessageReceptionMetric(name : String) : MessageReceptionMetric
}