package org.skellig.teststep.processor.performance.metrics.default

import org.skellig.teststep.processor.performance.metrics.DurationPercentileMetric
import org.skellig.teststep.processor.performance.metrics.MessageReceptionMetric
import org.skellig.teststep.processor.performance.metrics.MetricsFactory

class DefaultMetricsFactory : MetricsFactory {

    override fun createDurationPercentileMetric(name: String): DurationPercentileMetric =
        RequestDurationPercentileDefaultMetric(name)

    override fun createMessageReceptionMetric(name: String): MessageReceptionMetric =
        MessageReceptionDefaultMetric(name)
}