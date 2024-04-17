package org.skellig.teststep.processor.performance.metrics.prometheus

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.BaseUnits
import org.skellig.teststep.processor.performance.metrics.MessageReceptionMetric
import kotlin.math.ceil


/**
 * Represents a Prometheus metric for measuring message reception.
 *
 * @property name the name of the metric
 * @property meterRegistry the Prometheus [MeterRegistry] used for registering the metric
 */
open class MessageReceptionPrometheusMetric(private val name: String,
                                            meterRegistry: MeterRegistry) : MessageReceptionMetric {

    private val totalSuccessfulMessages =
        Counter.builder("$name total successful")
            .baseUnit(BaseUnits.MESSAGES)
            .description("Total successful messages for test: $name")
            .register(meterRegistry)
    private val totalFailedMessages =
        Counter.builder("$name total failed")
            .baseUnit(BaseUnits.MESSAGES)
            .description("Total failed messages for test: $name")
            .register(meterRegistry)

    private var startTime = -1L

    private fun getTotalElapsedSeconds(): Double {
        return ceil((System.currentTimeMillis() - startTime) / 1000.0)
    }

    override fun registerMessageReception() {
        registerStartTimeIfRequired()
        totalSuccessfulMessages.increment()
    }

    override fun registerMessageFailed() {
        registerStartTimeIfRequired()
        totalFailedMessages.increment()
    }

    override fun consumeTimeSeriesRecords(recordConsumer: (String) -> Unit) {
        recordConsumer("\nName: $name")
        recordConsumer("\nTotal requests: ${getTotalRequests()}")
        recordConsumer("\nMPS: ${getMps()}")
        startTime = -1
    }

    private fun registerStartTimeIfRequired() {
        if (startTime == -1L) {
            startTime = System.currentTimeMillis()
        }
    }

    private fun getMps() = getTotalRequests() / getTotalElapsedSeconds()

    private fun getTotalRequests() =
        totalSuccessfulMessages.count() + totalFailedMessages.count()
}