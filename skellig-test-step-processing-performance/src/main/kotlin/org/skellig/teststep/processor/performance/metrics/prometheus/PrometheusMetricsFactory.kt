package org.skellig.teststep.processor.performance.metrics.prometheus

import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.prometheus.client.CollectorRegistry
import org.skellig.teststep.processor.performance.metrics.DurationPercentileMetric
import org.skellig.teststep.processor.performance.metrics.MessageReceptionMetric
import org.skellig.teststep.processor.performance.metrics.MetricsFactory

/**
 * The PrometheusMetricsFactory class is responsible for creating metrics using PrometheusMeterRegistry from Micrometer library.
 * It implements the MetricsFactory interface and provides methods for creating DurationPercentileMetric and MessageReceptionMetric instances.
 *
 * @property meterRegistry The PrometheusMeterRegistry instance used for registering metrics
 * @property durationMetricsCache A cache for storing DurationPercentileMetric instances created using the createDurationPercentileMetric method
 * @property messageReceptionMetricsCache A cache for storing MessageReceptionMetric instances created using the createMessageReceptionMetric method
 */
class PrometheusMetricsFactory : MetricsFactory {

    companion object {
        private val meterRegistry = PrometheusMeterRegistry(
            PrometheusConfig.DEFAULT,
            CollectorRegistry.defaultRegistry,
            Clock.SYSTEM)
        private val durationMetricsCache = mutableMapOf<String, DurationPercentileMetric>()
        private val messageReceptionMetricsCache = mutableMapOf<String, MessageReceptionMetric>()

        init {
            meterRegistry.config().commonTags(listOf(Tag.of("application", "application")))
            ProcessorMetrics().bindTo(meterRegistry)
            JvmThreadMetrics().bindTo(meterRegistry)
            JvmMemoryMetrics().bindTo(meterRegistry)
            JvmGcMetrics().bindTo(meterRegistry)
        }
    }

    override fun createDurationPercentileMetric(name: String): DurationPercentileMetric =
        durationMetricsCache.computeIfAbsent(name) { RequestDurationPrometheusMetric(name, meterRegistry) }

    override fun createMessageReceptionMetric(name: String): MessageReceptionMetric =
        messageReceptionMetricsCache.computeIfAbsent(name) { MessageReceptionPrometheusMetric(name, meterRegistry) }
}