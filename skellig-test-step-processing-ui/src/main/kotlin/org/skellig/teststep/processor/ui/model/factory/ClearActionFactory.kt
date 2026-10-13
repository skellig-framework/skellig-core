package org.skellig.teststep.processor.ui.model.factory

import org.skellig.teststep.processor.ui.config.UiPageCatalog
import org.skellig.teststep.processor.ui.model.UiAction

internal class ClearActionFactory(private val pages: UiPageCatalog) : UiActionFactory<UiAction.Clear> {
    override val actionName = "clear"

    override fun create(definition: UiActionDefinition): UiAction.Clear {
        val target = definition.string("target")
        return UiAction.Clear(target, pages.xpath(target))
    }
}
