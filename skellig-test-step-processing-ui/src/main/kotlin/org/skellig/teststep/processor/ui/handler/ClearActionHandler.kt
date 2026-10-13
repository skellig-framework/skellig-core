package org.skellig.teststep.processor.ui.handler

import org.openqa.selenium.WebDriver
import org.skellig.teststep.processor.ui.model.UiAction
import java.time.Duration

internal class ClearActionHandler(timeout: Duration) : UiActionHandler<UiAction.Clear> {
    private val controls = UiControlWait(timeout)

    override fun handle(action: UiAction.Clear, driver: WebDriver) {
        val element = controls.interactable(driver, action.target, action.xpath)
        element.clear()
    }
}
