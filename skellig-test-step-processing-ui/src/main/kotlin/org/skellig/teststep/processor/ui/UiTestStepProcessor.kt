package org.skellig.teststep.processor.ui

import org.openqa.selenium.WebDriver
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processor.ui.handler.CompositeUiActionHandler
import org.skellig.teststep.processor.ui.model.UiTestStep
import java.nio.file.Path
import java.time.Duration

/** Executes once; readiness polling never repeats a click or the action sequence. */
internal class UiTestStepProcessor(
    timeout: Duration,
    private val createDriver: () -> WebDriver,
    screenshotsPath: Path
) : BaseTestStepProcessor<UiTestStep>() {
    private val screenshotCapturer = UiScreenshotCapturer(screenshotsPath)
    private var browser: WebDriver? = null
    private val uiActionsHandler = CompositeUiActionHandler(timeout)

    override fun getTestStepClass() = UiTestStep::class.java

    @Synchronized
    override fun processTestStep(testStep: UiTestStep): Any {
        val driver = driver()
        screenshotCapturer.captureBefore(driver, testStep.name)
        try {
            testStep.actions.forEach { action -> uiActionsHandler.handle(action, driver) }
        } finally {
            screenshotCapturer.captureAfter(driver, testStep.name)
        }

        return mapOf("url" to driver.currentUrl, "pages" to PageElementsStateCollector.collect(driver, testStep.actions))
    }

    @Synchronized
    override fun close() {
        try {
            browser?.quit()
        } finally {
            browser = null
            super.close()
        }
    }

    private fun driver(): WebDriver {
        return browser ?: createDriver().also { browser = it }
    }
}
