package org.skellig.teststep.processor.ui

import org.openqa.selenium.WebDriver
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processor.ui.handler.CompositeUiActionHandler
import org.skellig.teststep.processor.ui.model.UiTestStep
import org.skellig.teststep.processor.ui.observation.UiObservationCapture
import java.time.Duration

/** Executes once; readiness polling never repeats a click or the action sequence. */
internal class UiTestStepProcessor(
    state: TestScenarioState,
    timeout: Duration,
    private val createDriver: () -> WebDriver
) : BaseTestStepProcessor<UiTestStep>(state) {
    private var browser: WebDriver? = null
    private val uiActionsHandler = CompositeUiActionHandler(timeout)

    override fun getTestStepClass() = UiTestStep::class.java

    @Synchronized
    override fun processTestStep(testStep: UiTestStep): Any {
        val driver = driver()
        testStep.actions.forEach { action -> uiActionsHandler.handle(action, driver) }
        val observations = UiObservationCapture.capture(driver, testStep.observationTargets)
        return mapOf("url" to driver.currentUrl, "pages" to observations)
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
