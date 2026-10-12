package org.skellig.teststep.processor.ui.model.factory

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.factory.BaseDefaultTestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.processor.ui.config.UiPageCatalog
import org.skellig.teststep.processor.ui.model.UiAction
import org.skellig.teststep.processor.ui.model.UiTestStep
import org.skellig.teststep.reader.value.expression.ValueExpression

internal class UiTestStepFactory(
    registry: TestStepRegistry,
    expressions: ValueExpressionContextFactory,
    private val pages: UiPageCatalog
) : BaseDefaultTestStepFactory<UiTestStep>(registry, expressions) {
    // Unquoted hyphenated keys are parsed as subtraction expressions by the existing value parser.
    // Match their spelling without evaluating them, and also accept a quoted literal key.
    private fun actionsKey(rawTestStep: Map<ValueExpression, ValueExpression?>) =
        rawTestStep.keys.firstOrNull { it.toString().replace(" ", "") == "ui-actions" }

    override fun isConstructableFrom(rawTestStep: Map<ValueExpression, ValueExpression?>) = actionsKey(rawTestStep) != null

    override fun createTestStepBuilder(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): DefaultTestStep.Builder<UiTestStep> {
        val definitions = convertValue<Any>(rawTestStep[actionsKey(rawTestStep)], parameters) as? List<*>
            ?: error("ui-actions must be an ordered array of action maps")
        require(definitions.isNotEmpty()) { "ui-actions must contain at least one operation" }
        val actions = definitions.mapIndexed { index, value ->
            val fields = value as? Map<*, *> ?: error("UI action ${index + 1} must be a map")
            fun string(key: String): String = (fields[key] as? String)?.takeIf { it.isNotBlank() }
                ?: error("UI action ${index + 1} requires a non-empty '$key'")
            when (val operation = string("action")) {
                "navigate" -> {
                    require(fields.containsKey("page") xor fields.containsKey("url")) { "UI navigate action ${index + 1} requires exactly one of page or url" }
                    val url = if (fields.containsKey("page")) pages.pageUrl(string("page")) else UiPageCatalog.absoluteUrl(string("url")).toString()
                    UiAction.Navigate(url)
                }
                "click" -> string("target").let { UiAction.Click(it, pages.xpath(it)) }
                else -> error("Unsupported UI action '$operation' at position ${index + 1}")
            }
        }
        return UiTestStep.Builder(actions)
    }
}
