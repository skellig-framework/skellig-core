package org.skellig.teststep.processor.ui

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.skellig.teststep.processor.ui.model.UiAction

object PageElementsStateCollector {

    fun collect(driver: WebDriver, actions: List<UiAction>): Map<String, Map<String, UiElementState>> {
        val pages = linkedMapOf<String, MutableMap<String, UiElementState>>()
        extractUiElementNamesAndXPaths(actions).forEach { (target, xpath) ->
            val elements = driver.findElements(By.xpath(xpath))
            require(elements.size <= 1) { "Ambiguous UI control '$target': XPath '$xpath' matched ${elements.size} elements during observation capture" }

            val element = elements.singleOrNull()
            val state = UiElementState(
                present = (element != null),
                visible = (element?.isDisplayed ?: false),
                enabled = element?.isEnabled,
                selected = element?.isSelected,
                text = element?.text,
                value = element?.getDomProperty("value")
            )
            pages.getOrPut(target.substringBefore('.')) { linkedMapOf() }[target.substringAfter('.')] = state
        }
        return pages.mapValues { it.value }
    }

    private fun extractUiElementNamesAndXPaths(actions: List<UiAction>): Map<String, String> {
        val targets = actions.mapNotNull { action ->
            when (action) {
                is UiAction.Navigate -> null
                is UiAction.NavigableElement -> action.target to action.xpath
                else -> null
            }
        }.toMap()
        return targets
    }

    data class UiElementState(
        val present: Boolean,
        val visible: Boolean,
        val enabled: Boolean?,
        val selected: Boolean?,
        val text: String?,
        val value: String?
    )
}
