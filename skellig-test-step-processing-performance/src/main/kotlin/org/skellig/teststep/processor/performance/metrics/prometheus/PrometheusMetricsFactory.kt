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

class PrometheusMetricsFactory : MetricsFactory {

    private val meterRegistry = PrometheusMeterRegistry(
        PrometheusConfig.DEFAULT,
        CollectorRegistry.defaultRegistry,
        Clock.SYSTEM)

    init {
        meterRegistry.config().commonTags(listOf(Tag.of("application", "application")))
        ProcessorMetrics().bindTo(meterRegistry)
        JvmThreadMetrics().bindTo(meterRegistry)
        JvmMemoryMetrics().bindTo(meterRegistry)
        JvmGcMetrics().bindTo(meterRegistry)
    }

    override fun createDurationPercentileMetric(name: String): DurationPercentileMetric =
        RequestDurationPrometheusMetric(name, meterRegistry)

    override fun createMessageReceptionMetric(name: String): MessageReceptionMetric =
        MessageReceptionPrometheusMetric(name, meterRegistry)
}