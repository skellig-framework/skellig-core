package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.model.*
import org.skellig.teststep.processing.model.BaseValidationNode
import org.skellig.teststep.processing.model.GroupedValidationNode
import org.skellig.teststep.processing.model.PairValidationNode
import org.skellig.teststep.processing.model.SingleValidationNode
import org.skellig.teststep.processing.model.ValidationNodes
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.reader.value.expression.*

internal class ValidationNodeFactory(private val valueExpressionContextFactory: ValueExpressionContextFactory) {

    companion object {
        private val VALIDATE = AlphanumericValueExpression("validate")
    }

    fun create(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): ValidationNode? {
        return if (rawTestStep.containsKey(VALIDATE)) createRootValidationNode(rawTestStep[VALIDATE], parameters)
        else null
    }

    private fun createRootValidationNode(expectedResult: ValueExpression?, parameters: Map<String, Any?>): BaseValidationNode {
        return when (expectedResult) {
            is MapValueExpression -> ValidationNodes(createValidationNodes(expectedResult.value, parameters))
            is ListValueExpression -> ValidationNodes(createValidationNodes(expectedResult.value, parameters), true)
            else -> createValidationNode(expectedResult, parameters)
        }
    }

    private fun createValidationNode(expectedResult: Any?, parameters: Map<String, Any?>): BaseValidationNode {
        return when (expectedResult) {
            is MapValueExpression -> ValidationNodes(createValidationNodes(expectedResult.value, parameters))
            is ListValueExpression -> ValidationNodes(createValidationNodes(expectedResult.value, parameters), true)
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
            when (val expectedValueExpression = item.value as ValueExpression?) {
                is MapValueExpression -> GroupedValidationNode(actualValueExpression, createValidationNode(expectedValueExpression, parameters), parameters, valueExpressionContextFactory)
                is ListValueExpression -> GroupedValidationNode(actualValueExpression, createValidationNode(expectedValueExpression, parameters), parameters, valueExpressionContextFactory)
                else -> PairValidationNode(actualValueExpression, item.value as ValueExpression?, parameters, valueExpressionContextFactory)
            }
        }.toList()

}