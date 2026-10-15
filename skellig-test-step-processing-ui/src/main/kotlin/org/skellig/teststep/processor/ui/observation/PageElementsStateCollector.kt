package org.skellig.teststep.processor.ui.observation

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver

internal object PageElementsStateCollector {
    
    fun collect(driver: WebDriver, targets: Map<String, String>): Map<String, Map<String, Map<String, Any?>>> {
        val pages = linkedMapOf<String, MutableMap<String, Map<String, Any?>>>()
        targets.forEach { (target, xpath) ->
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
}
