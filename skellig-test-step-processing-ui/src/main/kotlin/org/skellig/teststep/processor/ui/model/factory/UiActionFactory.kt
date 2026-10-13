package org.skellig.teststep.processor.ui.model.factory

import org.skellig.teststep.processor.ui.model.UiAction

/** Creates a typed browser action without exposing its construction to the test step factory. */
internal interface UiActionFactory<out T : UiAction> {
    val actionName: String
    fun create(definition: UiActionDefinition): T
}
