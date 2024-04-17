package org.skellig.teststep.processor.rmq.config

import com.typesafe.config.Config
import org.skellig.teststep.processing.processor.config.ConfiguredTestStepProcessorDetails
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfig
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfigDetails
import org.skellig.teststep.processor.rmq.RmqConsumableTestStepProcessor
import org.skellig.teststep.processor.rmq.RmqTestStepProcessor
import org.skellig.teststep.processor.rmq.model.RmqConsumableTestStep
import org.skellig.teststep.processor.rmq.model.RmqTestStep
import org.skellig.teststep.processor.rmq.model.factory.RmqConsumableTestStepFactory
import org.skellig.teststep.processor.rmq.model.factory.RmqTestStepFactory

private const val RMQ_TEST_DATA_CONVERTER = "rmq.testData.converter"

/**
 * Represents the configuration for a RMQ test step processor.
 * This class implements the TestStepProcessorConfig interface and provides a way to configure a RMQ test step processor.
 */
class RmqTestStepProcessorConfig : TestStepProcessorConfig<RmqTestStep> {
    override fun config(details: TestStepProcessorConfigDetails): ConfiguredTestStepProcessorDetails<RmqTestStep>? {
        return if (details.config.hasPath("rmq"))
            ConfiguredTestStepProcessorDetails(
                RmqTestStepProcessor.Builder()
                    .rmqChannels(details.config)
                    .withTestScenarioState(details.state)
                    .build(),
                RmqTestStepFactory(
                    details.testStepRegistry,
                    details.valueExpressionContextFactory,
                    getDefaultTestDataConverter(details.config)
                )
            )
        else null
    }



}

/**
 * Represents the configuration for a RMQ test step processor.
 * This class implements the TestStepProcessorConfig interface and provides a way to configure a Consumable RMQ test step processor.
 */
class RmqConsumableTestStepProcessorConfig : TestStepProcessorConfig<RmqConsumableTestStep> {
    override fun config(details: TestStepProcessorConfigDetails): ConfiguredTestStepProcessorDetails<RmqConsumableTestStep>? {
        return if (details.config.hasPath("rmq"))
            ConfiguredTestStepProcessorDetails(
                RmqConsumableTestStepProcessor.Builder()
                    .rmqChannels(details.config)
                    .withTestScenarioState(details.state)
                    .build(),
                RmqConsumableTestStepFactory(
                    details.testStepRegistry,
                    details.valueExpressionContextFactory,
                    getDefaultTestDataConverter(details.config)
                )
            )
        else null
    }

}

private fun getDefaultTestDataConverter(config: Config): String? {
    return if (config.hasPath(RMQ_TEST_DATA_CONVERTER)) config.getString(RMQ_TEST_DATA_CONVERTER)
    else null
}