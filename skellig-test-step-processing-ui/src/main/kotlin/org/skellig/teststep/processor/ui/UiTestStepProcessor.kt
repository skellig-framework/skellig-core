package org.skellig.teststep.processor.ui

import org.openqa.selenium.By
import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.WebDriverWait
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processor.ui.model.UiAction
import org.skellig.teststep.processor.ui.model.UiTestStep
import java.time.Duration

/** Executes once; readiness polling never repeats a click or the action sequence. */
internal class UiTestStepProcessor(
    state: TestScenarioState,
    private val timeout: Duration,
    private val createDriver: () -> WebDriver
) : BaseTestStepProcessor<UiTestStep>(state) {
    private var browser: WebDriver? = null
    private var closed = false

    override fun getTestStepClass() = UiTestStep::class.java

    private fun driver(): WebDriver {
        check(!closed) { "UI processor is closed" }
        return browser ?: createDriver().also { browser = it }
    }

    @Synchronized
    override fun processTestStep(testStep: UiTestStep): Any {
        val driver = driver()
        testStep.actions.forEachIndexed { index, action ->
            try {
                when (action) {
                    is UiAction.Navigate -> driver.get(action.url)
                    is UiAction.Click -> {
                        val element = WebDriverWait(driver, timeout)
                            .ignoring(StaleElementReferenceException::class.java)
                            .until { browser ->
                                val matches = browser.findElements(By.xpath(action.xpath))
                                require(matches.size <= 1) { "Ambiguous UI control '${action.target}': XPath '${action.xpath}' matched ${matches.size} elements" }
                                matches.singleOrNull()?.takeIf { it.isDisplayed && it.isEnabled }
                            }
                        checkNotNull(element).click()
                    }
                }
            } catch (ex: Exception) {
                throw TestStepProcessingException("UI action ${index + 1} ($action) failed: ${ex.message}", ex)
            }
        }
        return mapOf("url" to driver.currentUrl)
    }

    @Synchronized
    override fun close() {
        if (closed) return
        closed = true
        val ownedBrowser = browser
        browser = null
        try {
            ownedBrowser?.quit()
        } finally {
            super.close()
        }
    }
}
