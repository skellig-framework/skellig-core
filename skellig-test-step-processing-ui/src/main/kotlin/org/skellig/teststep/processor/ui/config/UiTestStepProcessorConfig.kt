package org.skellig.teststep.processor.ui.config

import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
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
        val browser = ui.getConfig("browser")
        require(!browser.hasPath("mode") || browser.getString("mode") == "local") { "Only local browser execution is supported by this UI processor" }
        require(!browser.hasPath("name") || browser.getString("name") == "chrome") { "Only Chrome is supported by this UI processor" }
        val headless = browser.hasPath("headless") && browser.getBoolean("headless")
        val timeout = if (ui.hasPath("timeout")) ui.getDuration("timeout") else Duration.ofSeconds(30)
        require(!timeout.isZero && !timeout.isNegative) { "ui.timeout must be positive" }
        val pages = UiPageCatalog(ui)
        return ConfiguredTestStepProcessorDetails(
            UiTestStepProcessor(details.state, timeout) {
                val options = ChromeOptions()
                if (headless) options.addArguments("--headless=new")
                ChromeDriver(options)
            },
            UiTestStepFactory(details.testStepRegistry, details.valueExpressionContextFactory, pages)
        )
    }
}
