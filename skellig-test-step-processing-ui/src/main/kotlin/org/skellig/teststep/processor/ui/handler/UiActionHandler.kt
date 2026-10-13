package org.skellig.teststep.processor.ui.handler

import org.openqa.selenium.WebDriver
import org.skellig.teststep.processor.ui.model.UiAction

/** Handles one browser operation; the processor owns ordering and error reporting. */
internal interface UiActionHandler<in T : UiAction> {
    fun handle(action: T, driver: WebDriver)
}
