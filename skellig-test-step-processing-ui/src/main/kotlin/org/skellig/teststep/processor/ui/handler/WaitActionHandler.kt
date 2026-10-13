package org.skellig.teststep.processor.ui.handler

import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.WebDriverWait
import org.skellig.teststep.processor.ui.model.UiAction
import org.skellig.teststep.processor.ui.model.WaitCondition
import java.time.Duration

internal class WaitActionHandler(private val timeout: Duration) : UiActionHandler<UiAction.Wait> {
    private val controls = UiControlWait(timeout)

    override fun handle(action: UiAction.Wait, driver: WebDriver) {
        WebDriverWait(driver, timeout).ignoring(StaleElementReferenceException::class.java).until {
            val element = controls.unique(it, action.target, action.xpath)
            when (action.condition) {
                WaitCondition.PRESENT -> element != null
                WaitCondition.ABSENT -> element == null
                WaitCondition.VISIBLE -> element?.isDisplayed == true
                WaitCondition.HIDDEN -> element == null || !element.isDisplayed
                WaitCondition.ENABLED -> element?.isEnabled == true
            }
        }
    }
}
