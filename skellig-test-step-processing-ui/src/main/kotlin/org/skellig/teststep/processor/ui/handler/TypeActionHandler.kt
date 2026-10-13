package org.skellig.teststep.processor.ui.handler

import org.openqa.selenium.WebDriver
import org.skellig.teststep.processor.ui.model.UiAction
import java.time.Duration

internal class TypeActionHandler(timeout: Duration) : UiActionHandler<UiAction.Type> {
    private val controls = UiControlWait(timeout)

    override fun handle(action: UiAction.Type, driver: WebDriver) {
        val element = controls.interactable(driver, action.target, action.xpath)
        if (!action.append) element.clear()
        element.sendKeys(action.value)
    }
}
