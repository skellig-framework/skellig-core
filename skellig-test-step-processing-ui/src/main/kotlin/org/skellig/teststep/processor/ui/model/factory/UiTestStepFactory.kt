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
    pages: UiPageCatalog
) : BaseDefaultTestStepFactory<UiTestStep>(registry, expressions) {
    private val actionFactories: Map<String, UiActionFactory<UiAction>> = listOf(
        NavigateActionFactory(pages),
        ClickActionFactory(pages)
    ).associateBy { it.actionName }

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
            val definition = UiActionDefinition(fields, index + 1)
            val operation = definition.string("action")
            val factory = actionFactories[operation]
                ?: error("Unsupported UI action '$operation' at position ${definition.position}")
            factory.create(definition)
        }
        return UiTestStep.Builder(actions)
    }
}
