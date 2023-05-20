package org.skellig.teststep.processor.tcp.config

import org.skellig.teststep.processing.processor.config.ConfiguredTestStepProcessorDetails
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfig
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfigDetails
import org.skellig.teststep.processor.tcp.TcpConsumableTestStepProcessor
import org.skellig.teststep.processor.tcp.TcpTestStepProcessor
import org.skellig.teststep.processor.tcp.model.TcpConsumableTestStep
import org.skellig.teststep.processor.tcp.model.TcpTestStep
import org.skellig.teststep.processor.tcp.model.factory.TcpConsumableTestStepFactory
import org.skellig.teststep.processor.tcp.model.factory.TcpTestStepFactory

class TcpTestStepProcessorConfig : TestStepProcessorConfig<TcpTestStep> {
    override fun config(details: TestStepProcessorConfigDetails): ConfiguredTestStepProcessorDetails<TcpTestStep>? {
        return if (details.config.hasPath("tcp")) ConfiguredTestStepProcessorDetails(
            TcpTestStepProcessor.Builder()
                .tcpChannels(details.config)
                .withTestScenarioState(details.state)
                .withValidator(details.validator)
                .build(),
            TcpTestStepFactory(
                details.testStepRegistry,
                details.keywordProperties,
                details.testStepFactoryValueConverter
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
                .withValidator(details.validator)
                .build(),
            TcpConsumableTestStepFactory(
                details.testStepRegistry,
                details.keywordProperties,
                details.testStepFactoryValueConverter
            )
        )
        else null
    }
}