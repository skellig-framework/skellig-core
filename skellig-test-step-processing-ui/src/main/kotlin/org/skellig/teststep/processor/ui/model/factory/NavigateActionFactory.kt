package org.skellig.teststep.processor.ui.model.factory

import org.skellig.teststep.processor.ui.config.UiPageCatalog
import org.skellig.teststep.processor.ui.model.UiAction

internal class NavigateActionFactory(private val pages: UiPageCatalog) : UiActionFactory<UiAction.Navigate> {
    override val actionName = "navigate"

    override fun create(definition: UiActionDefinition): UiAction.Navigate {
        require(definition.has("page") xor definition.has("url")) {
            "UI navigate action ${definition.position} requires exactly one of page or url"
        }
        val url = if (definition.has("page")) pages.pageUrl(definition.string("page"))
        else UiPageCatalog.absoluteUrl(definition.string("url")).toString()
        return UiAction.Navigate(url)
    }
}
