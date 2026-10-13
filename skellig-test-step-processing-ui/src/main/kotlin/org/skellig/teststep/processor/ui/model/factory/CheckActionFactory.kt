package org.skellig.teststep.processor.ui.model.factory

import org.skellig.teststep.processor.ui.config.UiPageCatalog
import org.skellig.teststep.processor.ui.model.UiAction

internal class CheckActionFactory(private val pages: UiPageCatalog) : UiActionFactory<UiAction.Check> {
    override val actionName = "check"

    override fun create(definition: UiActionDefinition): UiAction.Check {
        val target = definition.string("target")
        return UiAction.Check(target, pages.xpath(target), definition.boolean("value"))
    }
}
