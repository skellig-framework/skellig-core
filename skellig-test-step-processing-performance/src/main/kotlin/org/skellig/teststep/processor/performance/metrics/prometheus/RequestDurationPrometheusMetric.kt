package org.skellig.teststep.processor.performance.metrics.prometheus

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import io.prometheus.client.Histogram
import org.skellig.teststep.processor.performance.metrics.DurationMetric
import org.skellig.teststep.processor.performance.metrics.DurationPercentileMetric
import java.time.Duration
import java.util.concurrent.TimeUnit


open class RequestDurationPrometheusMetric(private val name: String,
                                           meterRegistry: MeterRegistry) : DurationPercentileMetric {

    private val timer = Timer.builder("$name request duration")
        .description("Request duration for test: $name")
        .publishPercentileHistogram()
        .maximumExpectedValue(Duration.ofMillis(5000))
        .register(meterRegistry)

    private val metric = object : DurationMetric {
        override fun recordTime(time: Long) {
            timer.record(time, TimeUnit.MILLISECONDS)
        }
    }

    override fun createPercentileBucket(): DurationMetric = metric

    override fun close() {
        timer.close()
    }

    override fun consumeTimeSeriesRecords(recordConsumer: (String) -> Unit) {
        recordConsumer("Name: $name")
    }
}