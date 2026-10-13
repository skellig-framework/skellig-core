package org.skellig.teststep.processor.ui.handler

import org.openqa.selenium.By
import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.WebDriverWait
import org.skellig.teststep.processor.ui.model.UiAction
import java.time.Duration

internal class ClickActionHandler(private val timeout: Duration) : UiActionHandler<UiAction.Click> {
    override fun handle(action: UiAction.Click, driver: WebDriver) {
        val element = WebDriverWait(driver, timeout)
            .ignoring(StaleElementReferenceException::class.java)
            .until { browser ->
                val matches = browser.findElements(By.xpath(action.xpath))
                require(matches.size <= 1) { "Ambiguous UI control '${action.target}': XPath '${action.xpath}' matched ${matches.size} elements" }
                matches.singleOrNull()?.takeIf { it.isDisplayed && it.isEnabled }
            }
        element.click()
    }
}
