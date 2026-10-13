package org.skellig.teststep.processor.ui.model.factory

import org.skellig.teststep.processor.ui.config.UiPageCatalog
import org.skellig.teststep.processor.ui.model.UiAction
import org.skellig.teststep.processor.ui.model.SelectBy

internal class SelectActionFactory(private val pages: UiPageCatalog) : UiActionFactory<UiAction.Select> {
    override val actionName = "select"

    override fun create(definition: UiActionDefinition): UiAction.Select {
        val target = definition.string("target")
        val by = when (definition.string("by")) {
            "text" -> SelectBy.TEXT
            "value" -> SelectBy.VALUE
            else -> error("UI select action ${definition.position} requires by equal to text or value")
        }
        return UiAction.Select(target, pages.xpath(target), by, definition.text("value"))
    }
}
