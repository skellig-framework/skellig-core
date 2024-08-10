package org.skellig.teststep.processor.performance.metrics.default

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DefaultMetricsFactoryTest {

    @Test
    fun createPercentileMetrics() {
        assertEquals(RequestDurationPercentileDefaultMetric::class.java, DefaultMetricsFactory().createDurationPercentileMetric("m1")::class.java)
        assertEquals(MessageReceptionDefaultMetric::class.java, DefaultMetricsFactory().createMessageReceptionMetric("m2")::class.java)
    }
}