package org.skellig.teststep.processor.ui.model.factory

import org.skellig.teststep.processor.ui.config.UiPageCatalog
import org.skellig.teststep.processor.ui.model.UiAction

internal class ClickActionFactory(private val pages: UiPageCatalog) : UiActionFactory<UiAction.Click> {
    override val actionName = "click"

    override fun create(definition: UiActionDefinition): UiAction.Click {
        val target = definition.string("target")
        return UiAction.Click(target, pages.xpath(target))
    }
}
