package org.skellig.teststep.processor.ui.handler

import org.openqa.selenium.WebDriver
import org.skellig.teststep.processor.ui.model.UiAction
import java.time.Duration

internal class ClickActionHandler(timeout: Duration) : UiActionHandler<UiAction.Click> {
    private val controls = UiControlWait(timeout)

    override fun handle(action: UiAction.Click, driver: WebDriver) {
        controls.interactable(driver, action.target, action.xpath).click()
    }
}
