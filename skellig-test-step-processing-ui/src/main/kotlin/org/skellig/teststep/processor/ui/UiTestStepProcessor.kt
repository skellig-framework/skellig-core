package org.skellig.teststep.processor.ui

import org.openqa.selenium.WebDriver
import org.openqa.selenium.OutputType
import org.openqa.selenium.TakesScreenshot
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processor.ui.handler.CompositeUiActionHandler
import org.skellig.teststep.processor.ui.model.UiTestStep
import org.skellig.teststep.processing.util.logger
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** Executes once; readiness polling never repeats a click or the action sequence. */
internal class UiTestStepProcessor(
    state: TestScenarioState,
    timeout: Duration,
    private val createDriver: () -> WebDriver,
    private val screenshotsPath: Path
) : BaseTestStepProcessor<UiTestStep>(state) {
    private val log = logger<UiTestStepProcessor>()
    private val screenshotDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSSSSSSSS")
    private var browser: WebDriver? = null
    private val uiActionsHandler = CompositeUiActionHandler(timeout)

    override fun getTestStepClass() = UiTestStep::class.java

    @Synchronized
    override fun processTestStep(testStep: UiTestStep): Any {
        val driver = driver()
        captureScreenshot(driver, "before", testStep.name)
        try {
            testStep.actions.forEach { action -> uiActionsHandler.handle(action, driver) }
        } finally {
            captureScreenshot(driver, "after", testStep.name)
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

    private fun captureScreenshot(driver: WebDriver, phase: String, stepName: String) {
        try {
            val name = stepName.replace(Regex("[\\\\/:*?\"<>|\\p{Cntrl}]"), "_").take(120)
            val timestamp = LocalDateTime.now().format(screenshotDateFormat)
            val directory = screenshotsPath.toAbsolutePath().normalize()
            Files.createDirectories(directory)
            Files.write(directory.resolve("${phase}_${name}_$timestamp.png"),
                (driver as TakesScreenshot).getScreenshotAs(OutputType.BYTES), StandardOpenOption.CREATE_NEW)
        } catch (error: Exception) {
            log.warn("Could not save {} UI screenshot ({})", phase, error.javaClass.simpleName)
        }
    }

    private fun driver(): WebDriver {
        return browser ?: createDriver().also { browser = it }
    }
}
