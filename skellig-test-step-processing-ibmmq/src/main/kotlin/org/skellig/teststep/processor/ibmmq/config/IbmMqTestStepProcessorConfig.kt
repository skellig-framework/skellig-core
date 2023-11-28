package org.skellig.teststep.processor.ibmmq.config

import org.skellig.teststep.processing.processor.config.ConfiguredTestStepProcessorDetails
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfig
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfigDetails
import org.skellig.teststep.processor.ibmmq.IbmMqConsumableTestStepProcessor
import org.skellig.teststep.processor.ibmmq.IbmMqTestStepProcessor
import org.skellig.teststep.processor.ibmmq.model.IbmMqConsumableTestStep
import org.skellig.teststep.processor.ibmmq.model.IbmMqTestStep
import org.skellig.teststep.processor.ibmmq.model.factory.IbmMqConsumableTestStepFactory
import org.skellig.teststep.processor.ibmmq.model.factory.IbmMqTestStepFactory

class IbmMqTestStepProcessorConfig : TestStepProcessorConfig<IbmMqTestStep> {
    override fun config(details: TestStepProcessorConfigDetails): ConfiguredTestStepProcessorDetails<IbmMqTestStep>? {
        return if (details.config.hasPath("ibmmq"))
            ConfiguredTestStepProcessorDetails(
                IbmMqTestStepProcessor.Builder()
                    .ibmMqChannels(details.config)
                    .withTestScenarioState(details.state)
                    .build(),
                IbmMqTestStepFactory(
                    details.testStepRegistry,
                    details.valueExpressionContextFactory
                )
            )
        else null
    }

}

class IbmMqConsumableTestStepProcessorConfig : TestStepProcessorConfig<IbmMqConsumableTestStep> {
    override fun config(details: TestStepProcessorConfigDetails): ConfiguredTestStepProcessorDetails<IbmMqConsumableTestStep>? {
        return if (details.config.hasPath("ibmmq"))
            ConfiguredTestStepProcessorDetails(
                IbmMqConsumableTestStepProcessor.Builder()
                    .ibmMqChannels(details.config)
                    .withTestScenarioState(details.state)
                    .build(),
                IbmMqConsumableTestStepFactory(
                    details.testStepRegistry,
                    details.valueExpressionContextFactory
                )
            )
        else null
    }
}