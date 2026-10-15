package org.skellig.teststep.processor.ui.observation

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import java.util.Collections

/** Reads once after actions; the published result contains only detached maps and scalar values. */
internal object UiObservationCapture {
    fun capture(driver: WebDriver, targets: Map<String, String>): Map<String, Map<String, Map<String, Any?>>> {
        val pages = linkedMapOf<String, MutableMap<String, Map<String, Any?>>>()
        targets.forEach { (target, xpath) ->
            val matches = driver.findElements(By.xpath(xpath))
            require(matches.size <= 1) { "Ambiguous UI control '$target': XPath '$xpath' matched ${matches.size} elements during observation capture" }
            val element = matches.singleOrNull()
            val observation = mapOf(
                "present" to (element != null),
                "visible" to (element?.isDisplayed ?: false),
                "enabled" to element?.isEnabled,
                "selected" to element?.isSelected,
                "text" to element?.text,
                "value" to element?.getDomProperty("value")
            )
            pages.getOrPut(target.substringBefore('.')) { linkedMapOf() }[target.substringAfter('.')] = Collections.unmodifiableMap(observation)
        }
        return Collections.unmodifiableMap(pages.mapValues { Collections.unmodifiableMap(it.value) })
    }
}
