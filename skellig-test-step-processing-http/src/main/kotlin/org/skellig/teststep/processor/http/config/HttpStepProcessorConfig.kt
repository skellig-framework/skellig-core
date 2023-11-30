package org.skellig.teststep.processor.http.config

import org.skellig.teststep.processing.processor.config.ConfiguredTestStepProcessorDetails
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfig
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfigDetails
import org.skellig.teststep.processor.http.HttpTestStepProcessor
import org.skellig.teststep.processor.http.model.HttpTestStep
import org.skellig.teststep.processor.http.model.factory.HttpTestStepFactory

class HttpTestStepProcessorConfig : TestStepProcessorConfig<HttpTestStep> {
    override fun config(details: TestStepProcessorConfigDetails): ConfiguredTestStepProcessorDetails<HttpTestStep>? {
        return if (details.config.hasPath("http"))
            ConfiguredTestStepProcessorDetails(
                HttpTestStepProcessor.Builder()
                    .withHttpService(details.config)
                    .withTestScenarioState(details.state)
                    .build(),
                HttpTestStepFactory(
                    details.testStepRegistry,
                    details.valueExpressionContextFactory
                )
            )
        else null
    }

}