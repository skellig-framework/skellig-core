package org.skellig.teststep.processor.unix.config

import org.skellig.teststep.processing.processor.config.ConfiguredTestStepProcessorDetails
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfig
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfigDetails
import org.skellig.teststep.processor.unix.UnixShellTestStepProcessor
import org.skellig.teststep.processor.unix.model.UnixShellTestStep
import org.skellig.teststep.processor.unix.model.factory.UnixShellTestStepFactory

class UnixShellTestStepProcessorConfig : TestStepProcessorConfig<UnixShellTestStep> {
    override fun config(details: TestStepProcessorConfigDetails): ConfiguredTestStepProcessorDetails<UnixShellTestStep>? {
        return if (details.config.hasPath("unix")) ConfiguredTestStepProcessorDetails(
            UnixShellTestStepProcessor.Builder()
                .withHost(details.config)
                .withTestScenarioState(details.state)
                .withValidator(details.validator)
                .build(),
            UnixShellTestStepFactory(
                details.testStepRegistry,
                details.keywordProperties,
                details.testStepFactoryValueConverter
            )
        )
        else null
    }

}