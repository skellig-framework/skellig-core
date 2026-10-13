package org.skellig.teststep.processor.ui.handler

import org.openqa.selenium.WebDriver
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processor.ui.model.UiAction
import java.time.Duration

internal class CompositeUiActionHandler(timeout: Duration) : UiActionHandler<UiAction> {

    private val navigateActionHandler = NavigateActionHandler()
    private val clickActionHandler = ClickActionHandler(timeout)
    private val typeActionHandler = TypeActionHandler(timeout)
    private val clearActionHandler = ClearActionHandler(timeout)
    private val selectActionHandler = SelectActionHandler(timeout)
    private val checkActionHandler = CheckActionHandler(timeout)
    private val waitActionHandler = WaitActionHandler(timeout)

    override fun handle(action: UiAction, driver: WebDriver) {
        try {
            when (action) {
                is UiAction.Navigate -> navigateActionHandler.handle(action, driver)
                is UiAction.Wait -> waitActionHandler.handle(action, driver)
                is UiAction.Clear -> clearActionHandler.handle(action, driver)
                is UiAction.Select -> selectActionHandler.handle(action, driver)
                is UiAction.Check -> checkActionHandler.handle(action, driver)
                is UiAction.Type -> typeActionHandler.handle(action, driver)
                is UiAction.Click -> clickActionHandler.handle(action, driver)
            }
        } catch (ex: Exception) {
            throw TestStepProcessingException("UI action ($action) failed: ${ex.message}", ex)
        }
    }
}
