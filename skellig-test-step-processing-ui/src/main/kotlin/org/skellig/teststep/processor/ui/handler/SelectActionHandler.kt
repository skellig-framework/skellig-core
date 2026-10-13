package org.skellig.teststep.processor.ui.handler

import org.openqa.selenium.WebDriver
import org.skellig.teststep.processor.ui.model.UiAction
import org.openqa.selenium.support.ui.Select
import org.skellig.teststep.processor.ui.model.SelectBy
import java.time.Duration

internal class SelectActionHandler(timeout: Duration) : UiActionHandler<UiAction.Select> {
    private val controls = UiControlWait(timeout)

    override fun handle(action: UiAction.Select, driver: WebDriver) {
        val element = controls.interactable(driver, action.target, action.xpath)
        val select = Select(element)
        when (action.by) {
            SelectBy.TEXT -> select.selectByVisibleText(action.value)
            SelectBy.VALUE -> select.selectByValue(action.value)
        }
    }
}
