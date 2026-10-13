package org.skellig.teststep.processor.ui.handler

import org.openqa.selenium.By
import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

/** Poll readiness only; interactions are performed once after a unique control is ready. */
internal class UiControlWait(private val timeout: Duration) {
    fun interactable(driver: WebDriver, target: String, xpath: String): WebElement =
        WebDriverWait(driver, timeout).ignoring(StaleElementReferenceException::class.java).until {
            unique(it, target, xpath)?.takeIf { element -> element.isDisplayed && element.isEnabled }
        }

    fun unique(driver: WebDriver, target: String, xpath: String): WebElement? {
        val matches = driver.findElements(By.xpath(xpath))
        require(matches.size <= 1) { "Ambiguous UI control '$target': XPath '$xpath' matched ${matches.size} elements" }
        return matches.singleOrNull()
    }
}
