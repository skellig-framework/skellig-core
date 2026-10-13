package org.skellig.teststep.processor.ui.handler

import org.openqa.selenium.WebDriver
import org.skellig.teststep.processor.ui.model.UiAction
import java.time.Duration

internal class CheckActionHandler(timeout: Duration) : UiActionHandler<UiAction.Check> {
    private val controls = UiControlWait(timeout)

    override fun handle(action: UiAction.Check, driver: WebDriver) {
        val element = controls.interactable(driver, action.target, action.xpath)
        require(element.tagName.equals("input", true) && element.getDomAttribute("type").equals("checkbox", true)) {
            "UI check target '${action.target}' must be a checkbox"
        }
        if (element.isSelected != action.value) element.click()
    }
}
