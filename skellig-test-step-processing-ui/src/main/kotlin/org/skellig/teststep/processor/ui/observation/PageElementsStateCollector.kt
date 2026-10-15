package org.skellig.teststep.processor.ui.observation

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.skellig.teststep.processor.ui.model.UiAction

internal object PageElementsStateCollector {
    
    fun collect(driver: WebDriver, actions: List<UiAction>): Map<String, Map<String, Map<String, Any?>>> {
        val pages = linkedMapOf<String, MutableMap<String, Map<String, Any?>>>()
        extractUiElementNamesAndXPaths(actions).forEach { (target, xpath) ->
            val elements = driver.findElements(By.xpath(xpath))
            require(elements.size <= 1) { "Ambiguous UI control '$target': XPath '$xpath' matched ${elements.size} elements during observation capture" }

            val element = elements.singleOrNull()
            val observation = mapOf(
                "present" to (element != null),
                "visible" to (element?.isDisplayed ?: false),
                "enabled" to element?.isEnabled,
                "selected" to element?.isSelected,
                "text" to element?.text,
                "value" to element?.getDomProperty("value")
            )
            pages.getOrPut(target.substringBefore('.')) { linkedMapOf() }[target.substringAfter('.')] = observation
        }
        return pages.mapValues { it.value }
    }

    private fun extractUiElementNamesAndXPaths(actions: List<UiAction>): Map<String, String> {
        val targets = actions.mapNotNull { action ->
            when (action) {
                is UiAction.Navigate -> null
                is UiAction.Type -> action.target to action.xpath
                is UiAction.Clear -> action.target to action.xpath
                is UiAction.Select -> action.target to action.xpath
                is UiAction.Check -> action.target to action.xpath
                is UiAction.Wait -> action.target to action.xpath
                is UiAction.Click -> action.target to action.xpath
            }
        }.toMap()
        return targets
    }
}
