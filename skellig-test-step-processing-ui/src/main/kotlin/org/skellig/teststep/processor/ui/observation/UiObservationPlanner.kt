package org.skellig.teststep.processor.ui.observation

import org.skellig.teststep.processor.ui.config.UiPageCatalog
import org.skellig.teststep.reader.value.expression.*

/** Plans dependencies without executing state updaters, validation, or user functions. */
internal class UiObservationPlanner(private val pages: UiPageCatalog) {
    private val parameterReference = Regex("\\$\\{[A-Za-z_][A-Za-z_0-9]*}")

    fun plan(raw: Map<ValueExpression, ValueExpression?>, resultKey: String): Map<String, String> {
        val dependencies = Dependencies(resultKey)
        (raw[AlphanumericValueExpression("state")] as? MapValueExpression)?.value?.values?.forEach {
            dependencies.expression(it, null)
        }
        dependencies.validation(raw[AlphanumericValueExpression("validate")], emptyList())
        return dependencies.targets.associateWith { pages.xpath(it) }
    }

    private inner class Dependencies(private val resultKey: String) {
        val targets = linkedSetOf<String>()

        private fun include(path: List<String>) {
            if (path.isEmpty()) targets.addAll(pages.targets())
            else if (path.first() == "pages") {
                when (path.size) {
                    1 -> targets.addAll(pages.targets())
                    2 -> targets.addAll(pages.targets(path[1]))
                    else -> targets.add("${path[1]}.${path[2]}")
                }
            }
        }

        fun validation(node: ValueExpression?, scope: List<String>?) {
            when (node) {
                is MapValueExpression -> node.value.forEach { (actual, expected) ->
                    val grouped = expected is MapValueExpression || expected is ListValueExpression
                    val childScope = expression(actual, scope, !grouped)
                    if (grouped) validation(expected, childScope)
                    else expression(expected, null, currentScope = scope)
                }
                is ListValueExpression -> node.value.forEach { validation(it, scope) }
                else -> expression(node, scope)
            }
        }

        fun expression(
            node: ValueExpression?, scope: List<String>?, observe: Boolean = true,
            currentScope: List<String>? = scope
        ): List<String>? {
            when (node) {
                is CallChainExpression -> {
                    var path = scope
                    node.callChain.forEach { part ->
                        path = segment(part, path, currentScope)
                    }
                    if (observe) path?.let { include(it) }
                    return path
                }
                is AlphanumericValueExpression, is StringValueExpression, is FunctionCallExpression -> {
                    val path = segment(node, scope, currentScope)
                    if (observe) path?.let { include(it) }
                    return path
                }
                is MapValueExpression -> node.value.forEach { (key, value) -> expression(key, null, currentScope = currentScope); expression(value, null, currentScope = currentScope) }
                is ListValueExpression -> node.value.forEach { expression(it, null, currentScope = currentScope) }
                is PropertyValueExpression -> {
                    // A simple parameter lookup cannot read this step's result; compound keys remain conservative.
                    if (!parameterReference.matches(node.toString())) include(scope ?: emptyList())
                }
                null, is BooleanValueExpression, is NumberValueExpression, is PatternValueExpression, is AnyValueExpression -> Unit
                // Dynamic references, operators and other opaque expressions may consume any part of the result.
                else -> include(scope ?: emptyList())
            }
            return null
        }

        private fun segment(node: ValueExpression?, scope: List<String>?, currentScope: List<String>?): List<String>? {
            return when (node) {
                is AlphanumericValueExpression, is StringValueExpression -> when (val name = node.toString()) {
                    "$" -> currentScope ?: emptyList()
                    "\$result" -> emptyList()
                    else -> scope?.plus(name)
                }
                is FunctionCallExpression -> {
                    node.args.forEach { expression(it, null, currentScope = currentScope) }
                    if (node.name == "get") {
                        val key = node.args.firstOrNull()
                        if (key is AlphanumericValueExpression || key is StringValueExpression) {
                            if (key.toString() == resultKey) emptyList() else null
                        } else {
                            include(emptyList())
                            null
                        }
                    } else {
                        include(scope ?: emptyList())
                        null
                    }
                }
                else -> {
                    include(scope ?: emptyList())
                    null
                }
            }
        }
    }
}
