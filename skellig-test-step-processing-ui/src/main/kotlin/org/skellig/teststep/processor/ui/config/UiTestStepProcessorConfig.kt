package org.skellig.teststep.processor.ui.config

import org.skellig.teststep.processing.processor.config.ConfiguredTestStepProcessorDetails
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfig
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfigDetails
import org.skellig.teststep.processor.ui.UiTestStepProcessor
import org.skellig.teststep.processor.ui.model.UiTestStep
import org.skellig.teststep.processor.ui.model.factory.UiTestStepFactory
import java.time.Duration

/** Discovered by the standard Skellig processor configuration scan. */
class UiTestStepProcessorConfig : TestStepProcessorConfig<UiTestStep> {
    override fun config(details: TestStepProcessorConfigDetails): ConfiguredTestStepProcessorDetails<UiTestStep>? {
        if (!details.config.hasPath("ui")) return null
        val ui = details.config.getConfig("ui")
        val browser = UiBrowserSessionFactory(ui.getConfig("browser"))
        val timeout = if (ui.hasPath("timeout")) ui.getDuration("timeout") else Duration.ofSeconds(30)
        require(!timeout.isZero && !timeout.isNegative) { "ui.timeout must be positive" }
        val pages = UiPageCatalog(ui)
        return ConfiguredTestStepProcessorDetails(
            UiTestStepProcessor(details.state, timeout, browser::create),
            UiTestStepFactory(details.testStepRegistry, details.valueExpressionContextFactory, pages)
        )
    }
}
