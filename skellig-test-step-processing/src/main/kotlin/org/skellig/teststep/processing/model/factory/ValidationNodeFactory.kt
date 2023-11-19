package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.validation.*
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.reader.sts.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.sts.value.expression.ValueExpression

class ValidationNodeFactory(private val valueExpressionContextFactory: ValueExpressionContextFactory) {

    companion object {
        private const val VALIDATE = "validate"
    }

    fun create(rawTestStep: Map<Any, Any?>, parameters: Map<String, Any?>): ValidationNode? {
        return if (rawTestStep.containsKey(VALIDATE)) createValidationNode(rawTestStep[VALIDATE], parameters)
        else null
    }

    private fun createValidationNode(expectedResult: Any?, parameters: Map<String, Any?>): ValidationNode {
        return when (expectedResult) {
            is Map<*, *> -> {
                ValidationNodes(expectedResult.map { item ->
                    val actualValueExpression = item.key as ValueExpression
                    when (item.value) {
                        is Map<*, *> -> GroupedValidationNode(actualValueExpression, createValidationNode(item.value as Map<*, *>, parameters), parameters, valueExpressionContextFactory)
                        is List<*> -> GroupedValidationNode(actualValueExpression, createValidationNode(item.value as List<*>, parameters), parameters, valueExpressionContextFactory)
                        else -> PairValidationNode(actualValueExpression, item.value as ValueExpression?, parameters, valueExpressionContextFactory)
                    }
                }.toList())
            }

            is Collection<*> -> ValidationNodes(expectedResult.map { createValidationNode(it, parameters) }.toList())
            is ValueExpression -> SingleValidationNode(expectedResult, parameters, valueExpressionContextFactory)
            else -> SingleValidationNode(
                expectedResult?.let { AlphanumericValueExpression(expectedResult.toString()) }, parameters, valueExpressionContextFactory
            )
        }
    }

}