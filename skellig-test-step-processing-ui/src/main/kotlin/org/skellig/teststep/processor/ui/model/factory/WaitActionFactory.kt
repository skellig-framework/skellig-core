package org.skellig.teststep.processor.ui.model.factory

import org.skellig.teststep.processor.ui.config.UiPageCatalog
import org.skellig.teststep.processor.ui.model.UiAction
import org.skellig.teststep.processor.ui.model.WaitCondition

internal class WaitActionFactory(private val pages: UiPageCatalog) : UiActionFactory<UiAction.Wait> {
    override val actionName = "wait"

    override fun create(definition: UiActionDefinition): UiAction.Wait {
        val target = definition.string("target")
        val condition = definition.string("condition")
        val state = WaitCondition.entries.find { it.name.lowercase() == condition }
            ?: error("Unsupported UI wait condition '$condition' at position ${definition.position}")
        return UiAction.Wait(target, pages.xpath(target), state)
    }
}
