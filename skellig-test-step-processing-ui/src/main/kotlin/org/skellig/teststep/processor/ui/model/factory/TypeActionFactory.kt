package org.skellig.teststep.processor.ui.model.factory

import org.skellig.teststep.processor.ui.config.UiPageCatalog
import org.skellig.teststep.processor.ui.model.UiAction

internal class TypeActionFactory(private val pages: UiPageCatalog) : UiActionFactory<UiAction.Type> {
    override val actionName = "type"

    override fun create(definition: UiActionDefinition): UiAction.Type {
        val target = definition.string("target")
        return UiAction.Type(target, pages.xpath(target), definition.text("value"), definition.boolean("append", false))
    }
}
