package org.skellig.teststep.processor.wiremock.config

import org.skellig.teststep.processing.processor.config.ConfiguredTestStepProcessorDetails
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfig
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfigDetails
import org.skellig.teststep.processor.wiremock.WireMockTestStepProcessor
import org.skellig.teststep.processor.wiremock.model.WireMockTestStep
import org.skellig.teststep.processor.wiremock.model.factory.WireMockTestStepFactory

/** Discovered by the standard Skellig processor configuration scan; inactive without a `wiremock` key. */
class WireMockTestStepProcessorConfig : TestStepProcessorConfig<WireMockTestStep> {
    override fun config(details: TestStepProcessorConfigDetails): ConfiguredTestStepProcessorDetails<WireMockTestStep>? {
        if (!details.config.hasPath("wiremock")) return null
        val servers = MockServerSettings.readAll(details.config.getConfig("wiremock"))
        return ConfiguredTestStepProcessorDetails(
            WireMockTestStepProcessor.start(details.state, servers),
            WireMockTestStepFactory(details.testStepRegistry, details.valueExpressionContextFactory)
        )
    }
}
