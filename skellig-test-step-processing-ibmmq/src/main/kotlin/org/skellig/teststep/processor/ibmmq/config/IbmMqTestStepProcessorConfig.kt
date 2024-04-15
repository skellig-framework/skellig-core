package org.skellig.teststep.processor.ibmmq.config

import com.typesafe.config.Config
import org.skellig.teststep.processing.processor.config.ConfiguredTestStepProcessorDetails
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfig
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfigDetails
import org.skellig.teststep.processor.ibmmq.IbmMqConsumableTestStepProcessor
import org.skellig.teststep.processor.ibmmq.IbmMqTestStepProcessor
import org.skellig.teststep.processor.ibmmq.model.IbmMqConsumableTestStep
import org.skellig.teststep.processor.ibmmq.model.IbmMqTestStep
import org.skellig.teststep.processor.ibmmq.model.factory.IbmMqConsumableTestStepFactory
import org.skellig.teststep.processor.ibmmq.model.factory.IbmMqTestStepFactory

private const val IBMMQ_TEST_DATA_CONVERTER = "ibmmq.testData.converter"

/**
 * Represents the configuration for a IBMMQ test step processor.
 * This class implements the TestStepProcessorConfig interface and provides a way to configure a IBMMQ test step processor.
 */
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
                    details.valueExpressionContextFactory,
                    getDefaultTestDataConverter(details.config)
                )
            )
        else null
    }

}

/**
 * Represents the configuration for a IBMMQ test step processor.
 * This class implements the TestStepProcessorConfig interface and provides a way to configure a Consumable IBMMQ test step processor.
 */
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
                    details.valueExpressionContextFactory,
                    getDefaultTestDataConverter(details.config)
                )
            )
        else null
    }
}

private fun getDefaultTestDataConverter(config: Config): String? {
    return if (config.hasPath(IBMMQ_TEST_DATA_CONVERTER)) config.getString(IBMMQ_TEST_DATA_CONVERTER)
    else null
}