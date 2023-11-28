package org.skellig.teststep.processor.performance.config

import org.skellig.teststep.processing.processor.config.ConfiguredTestStepProcessorDetails
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfig
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfigDetails
import org.skellig.teststep.processor.performance.PerformanceTestStepProcessor
import org.skellig.teststep.processor.performance.metrics.default.DefaultMetricsFactory
import org.skellig.teststep.processor.performance.metrics.prometheus.PrometheusMetricsFactory
import org.skellig.teststep.processor.performance.model.PerformanceTestStep
import org.skellig.teststep.processor.performance.model.factory.PerformanceTestStepFactory

class PerformanceTestStepProcessorConfig : TestStepProcessorConfig<PerformanceTestStep> {

    override fun config(details: TestStepProcessorConfigDetails): ConfiguredTestStepProcessorDetails<PerformanceTestStep> {
        val metricsFactory =
            if (details.config.getString("performance.metrics") == "prometheus") PrometheusMetricsFactory()
            else DefaultMetricsFactory()

        return ConfiguredTestStepProcessorDetails(
            PerformanceTestStepProcessor.Builder()
                .metricsFactory(metricsFactory)
                .testStepRegistry(details.testStepRegistry)
                .testStepProcessor(details.testStepProcessor)
                .build(),
            PerformanceTestStepFactory(
                details.testStepFactory,
                details.valueExpressionContextFactory
            )
        )
    }
}