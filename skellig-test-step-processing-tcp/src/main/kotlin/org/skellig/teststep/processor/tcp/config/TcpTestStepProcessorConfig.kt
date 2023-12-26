package org.skellig.teststep.processor.tcp.config

import com.typesafe.config.Config
import org.skellig.teststep.processing.processor.config.ConfiguredTestStepProcessorDetails
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfig
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfigDetails
import org.skellig.teststep.processor.tcp.TcpConsumableTestStepProcessor
import org.skellig.teststep.processor.tcp.TcpTestStepProcessor
import org.skellig.teststep.processor.tcp.model.TcpConsumableTestStep
import org.skellig.teststep.processor.tcp.model.TcpTestStep
import org.skellig.teststep.processor.tcp.model.factory.TcpConsumableTestStepFactory
import org.skellig.teststep.processor.tcp.model.factory.TcpTestStepFactory

private const val TCP_TEST_DATA_CONVERTER = "tcp.testData.converter"

class TcpTestStepProcessorConfig : TestStepProcessorConfig<TcpTestStep> {
    override fun config(details: TestStepProcessorConfigDetails): ConfiguredTestStepProcessorDetails<TcpTestStep>? {
        return if (details.config.hasPath("tcp")) ConfiguredTestStepProcessorDetails(
            TcpTestStepProcessor.Builder()
                .tcpChannels(details.config)
                .withTestScenarioState(details.state)
                .build(),
            TcpTestStepFactory(
                details.testStepRegistry,
                details.valueExpressionContextFactory,
                getDefaultTestDataConverter(details.config)
            )
        )
        else null
    }

}

class TcpConsumableTestStepProcessorConfig : TestStepProcessorConfig<TcpConsumableTestStep> {
    override fun config(details: TestStepProcessorConfigDetails): ConfiguredTestStepProcessorDetails<TcpConsumableTestStep>? {
        return if (details.config.hasPath("tcp")) ConfiguredTestStepProcessorDetails(
            TcpConsumableTestStepProcessor.Builder()
                .tcpChannels(details.config)
                .withTestScenarioState(details.state)
                .build(),
            TcpConsumableTestStepFactory(
                details.testStepRegistry,
                details.valueExpressionContextFactory,
                getDefaultTestDataConverter(details.config)
            )
        )
        else null
    }
}

private fun getDefaultTestDataConverter(config: Config): String? {
    return if (config.hasPath(TCP_TEST_DATA_CONVERTER)) config.getString(TCP_TEST_DATA_CONVERTER)
    else null
}