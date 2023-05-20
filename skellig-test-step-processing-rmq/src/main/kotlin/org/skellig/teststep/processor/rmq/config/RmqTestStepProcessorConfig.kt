package org.skellig.teststep.processor.rmq.config

import org.skellig.teststep.processing.processor.config.ConfiguredTestStepProcessorDetails
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfig
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfigDetails
import org.skellig.teststep.processor.rmq.RmqConsumableTestStepProcessor
import org.skellig.teststep.processor.rmq.RmqTestStepProcessor
import org.skellig.teststep.processor.rmq.model.RmqConsumableTestStep
import org.skellig.teststep.processor.rmq.model.RmqTestStep
import org.skellig.teststep.processor.rmq.model.factory.RmqConsumableTestStepFactory
import org.skellig.teststep.processor.rmq.model.factory.RmqTestStepFactory

class RmqTestStepProcessorConfig : TestStepProcessorConfig<RmqTestStep> {
    override fun config(details: TestStepProcessorConfigDetails): ConfiguredTestStepProcessorDetails<RmqTestStep>? {
        return if (details.config.hasPath("rmq"))
            ConfiguredTestStepProcessorDetails(
                RmqTestStepProcessor.Builder()
                    .rmqChannels(details.config)
                    .withTestScenarioState(details.state)
                    .withValidator(details.validator)
                    .build(),
                RmqTestStepFactory(
                    details.testStepRegistry,
                    details.keywordProperties,
                    details.testStepFactoryValueConverter
                )
            )
        else null
    }

}

class RmqConsumableTestStepProcessorConfig : TestStepProcessorConfig<RmqConsumableTestStep> {
    override fun config(details: TestStepProcessorConfigDetails): ConfiguredTestStepProcessorDetails<RmqConsumableTestStep>? {
        return if (details.config.hasPath("rmq"))
            ConfiguredTestStepProcessorDetails(
                RmqConsumableTestStepProcessor.Builder()
                    .rmqChannels(details.config)
                    .withTestScenarioState(details.state)
                    .withValidator(details.validator)
                    .build(),
                RmqConsumableTestStepFactory(
                    details.testStepRegistry,
                    details.keywordProperties,
                    details.testStepFactoryValueConverter
                )
            )
        else null
    }

}