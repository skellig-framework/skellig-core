package org.skellig.teststep.processor.ui.handler

import org.openqa.selenium.WebDriver
import org.skellig.teststep.processor.ui.model.UiAction

internal class NavigateActionHandler : UiActionHandler<UiAction.Navigate> {
    override fun handle(action: UiAction.Navigate, driver: WebDriver) {
        driver.get(action.url)
    }
}
