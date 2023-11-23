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
        return if (rawTestStep.containsKey(VALIDATE)) createRootValidationNode(rawTestStep[VALIDATE], parameters)
        else null
    }

    private fun createRootValidationNode(expectedResult: Any?, parameters: Map<String, Any?>): ValidationNode {
        return when (expectedResult) {
            is Map<*, *> -> RootValidationNodes(createValidationNodes(expectedResult, parameters))
            is Collection<*> -> RootValidationNodes(createValidationNodes(expectedResult, parameters))
            else -> createValidationNode(expectedResult, parameters)
        }
    }

    private fun createValidationNode(expectedResult: Any?, parameters: Map<String, Any?>): ValidationNode {
        return when (expectedResult) {
            is Map<*, *> -> ValidationNodes(createValidationNodes(expectedResult, parameters))
            is Collection<*> -> ValidationNodes(createValidationNodes(expectedResult, parameters))
            is ValueExpression -> SingleValidationNode(expectedResult, parameters, valueExpressionContextFactory)
            else -> SingleValidationNode(
                expectedResult?.let { AlphanumericValueExpression(expectedResult.toString()) }, parameters, valueExpressionContextFactory
            )
        }
    }

    private fun createValidationNodes(expectedResult: Collection<*>, parameters: Map<String, Any?> ) =
        expectedResult.map { createValidationNode(it, parameters) }.toList()

    private fun createValidationNodes(expectedResult: Map<*, *>, parameters: Map<String, Any?>) =
        expectedResult.map { item ->
            val actualValueExpression = item.key as ValueExpression
            when (item.value) {
                is Map<*, *> -> GroupedValidationNode(actualValueExpression, createValidationNode(item.value as Map<*, *>, parameters), parameters, valueExpressionContextFactory)
                is List<*> -> GroupedValidationNode(actualValueExpression, createValidationNode(item.value as List<*>, parameters), parameters, valueExpressionContextFactory)
                else -> PairValidationNode(actualValueExpression, item.value as ValueExpression?, parameters, valueExpressionContextFactory)
            }
        }.toList()

}