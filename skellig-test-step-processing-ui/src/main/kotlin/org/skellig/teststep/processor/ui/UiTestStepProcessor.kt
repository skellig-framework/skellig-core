package org.skellig.teststep.processor.ui

import org.openqa.selenium.WebDriver
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processor.ui.handler.ClickActionHandler
import org.skellig.teststep.processor.ui.handler.NavigateActionHandler
import org.skellig.teststep.processor.ui.model.UiAction
import org.skellig.teststep.processor.ui.model.UiTestStep
import java.time.Duration

/** Executes once; readiness polling never repeats a click or the action sequence. */
internal class UiTestStepProcessor(
    state: TestScenarioState,
    timeout: Duration,
    private val createDriver: () -> WebDriver
) : BaseTestStepProcessor<UiTestStep>(state) {
    private var browser: WebDriver? = null
    private val navigateActionHandler = NavigateActionHandler()
    private val clickActionHandler = ClickActionHandler(timeout)

    override fun getTestStepClass() = UiTestStep::class.java

    private fun driver(): WebDriver {
        return browser ?: createDriver().also { browser = it }
    }

    @Synchronized
    override fun processTestStep(testStep: UiTestStep): Any {
        val driver = driver()
        testStep.actions.forEachIndexed { index, action ->
            try {
                when (action) {
                    is UiAction.Navigate -> navigateActionHandler.handle(action, driver)
                    is UiAction.Click -> clickActionHandler.handle(action, driver)
                }
            } catch (ex: Exception) {
                throw TestStepProcessingException("UI action ${index + 1} ($action) failed: ${ex.message}", ex)
            }
        }
        return mapOf("url" to driver.currentUrl)
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
}
