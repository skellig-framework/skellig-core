package org.skellig.teststep.processing.validation

import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.reader.sts.value.expression.*

interface ValidationNode {
    fun compare(value: Any?, parameters: Map<String, Any?>): Boolean
}

class SingleValidationNode(val expected: ValueExpression,
                           private val valueExpressionContextFactory: ValueExpressionContextFactory) : ValidationNode {

    override fun compare(value: Any?, parameters: Map<String, Any?>): Boolean {
        return expected.evaluate(valueExpressionContextFactory.createForValidation(value, parameters)) as Boolean
    }

}

class PairValidationNode(val actual: ValueExpression, val expected: ValueExpression,
                         private val valueExpressionContextFactory: ValueExpressionContextFactory) : ValidationNode {

    override fun compare(value: Any?, parameters: Map<String, Any?>): Boolean {
        val contextForActual = valueExpressionContextFactory.createForValidation(value, parameters)
        val contextForExpected = valueExpressionContextFactory.createForValidation(parameters, value)

        return actual.evaluate(contextForActual) == expected.evaluate(contextForExpected)
    }
}

class GroupedValidationNode(val actual: ValueExpression, val items: List<ValidationNode>,
                            private val valueExpressionContextFactory: ValueExpressionContextFactory) : ValidationNode {

    companion object {
        private const val ANY_MATCH = "anyMatch"
        private const val NONE_MATCH = "noneMatch"
        private const val DEFAULT_MATCHING = "allMatch"
    }

    override fun compare(value: Any?, parameters: Map<String, Any?>): Boolean {
        val evaluatedActualValue = actual.evaluate(valueExpressionContextFactory.createForValidation(value, parameters))

        return if (getMatching() == ANY_MATCH) {
            if (evaluatedActualValue is List<*>) {
                evaluatedActualValue.any { v -> items.all { n -> n.compare(v, parameters) } }
            } else {
                items.any { it.compare(evaluatedActualValue, parameters) }
            }
        } else if (getMatching() == NONE_MATCH) {
            if (evaluatedActualValue is List<*>) {
                evaluatedActualValue.none { v -> items.all { n -> n.compare(v, parameters) } }
            } else {
                items.none { it.compare(evaluatedActualValue, parameters) }
            }
        } else {
            if (evaluatedActualValue is List<*>) {
                evaluatedActualValue.all { v -> items.all { n -> n.compare(v, parameters) } }
            } else {
                items.all { it.compare(evaluatedActualValue, parameters) }
            }
        }
    }

    private fun getMatching(): String {
        return when (actual) {
            is CallChainExpression -> actual.callChain.last().toString()
            is AlphanumericValueExpression -> actual.toString()
            else -> DEFAULT_MATCHING
        }
    }
}