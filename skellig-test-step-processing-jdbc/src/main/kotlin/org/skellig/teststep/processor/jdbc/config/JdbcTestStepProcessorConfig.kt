package org.skellig.teststep.processor.jdbc.config

import org.skellig.teststep.processing.processor.config.ConfiguredTestStepProcessorDetails
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfig
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfigDetails
import org.skellig.teststep.processor.jdbc.JdbcTestStepProcessor
import org.skellig.teststep.processor.jdbc.model.JdbcTestStep
import org.skellig.teststep.processor.jdbc.model.factory.JdbcTestStepFactory

class JdbcTestStepProcessorConfig : TestStepProcessorConfig<JdbcTestStep> {
    override fun config(details: TestStepProcessorConfigDetails): ConfiguredTestStepProcessorDetails<JdbcTestStep>? {
        return if (details.config.hasPath("jdbc"))
            ConfiguredTestStepProcessorDetails(
                JdbcTestStepProcessor.Builder()
                    .withDbServers(details.config)
                    .withTestScenarioState(details.state)
                    .build(),
                JdbcTestStepFactory(
                    details.testStepRegistry,
                    details.valueExpressionContextFactory
                )
            )
        else null
    }
}