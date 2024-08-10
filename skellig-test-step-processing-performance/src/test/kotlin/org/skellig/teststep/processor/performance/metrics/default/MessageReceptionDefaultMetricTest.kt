package org.skellig.teststep.processor.performance.metrics.default

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test


class MessageReceptionDefaultMetricTest {

    @Test
    fun `register failed and success message reception`() {
        val metric = MessageReceptionDefaultMetric("m1")

        metric.registerMessageSuccess()
        metric.registerMessageSuccess()
        metric.registerMessageFailed()

        var log = ""
        metric.consumeTimeSeriesRecords { log += it }

        assertTrue(log.matches(Regex("\nname: m1\n" +
                ".+\n" +
                "total passed: 2\n" +
                ".+\n" +
                "total failed: 1")))
    }
}