package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.model.*
import org.skellig.teststep.processing.model.BaseValidationNode
import org.skellig.teststep.processing.model.GroupedValidationNode
import org.skellig.teststep.processing.model.PairValidationNode
import org.skellig.teststep.processing.model.SingleValidationNode
import org.skellig.teststep.processing.model.ValidationNodes
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.reader.value.expression.*

/**
 * The `ValidationNodeFactory` class is responsible for creating [ValidationNode]s based on the provided test step.
 * It uses the internal implementations of [ValidationNode] interface and composes the tree-like structure of validation nodes.
 *
 * The internal implementation of validation works the following way:
 * 1) Use a result from processing of a test step as a value in [ValidationNode.validate]
 * 2) This value goes down the root in the tree and each validation node can extract other values from the original one,
 * by calling [ValueExpression] on it, defined in a property on the left (ex. key of the node).
 * Each property can be a [AlphanumericValueExpression], [FunctionCallExpression], [CallChainExpression], etc. as long as
 * it's applicable to a value. For example, let's assume you have 'validations' like this:
 *```
 * service_1.jsonToMap().items[0] {
 *   accounts.fromIndex(0) {
 *     name = "Acc A"
 *     email = accA@mail.com
 *   }
 * }
 *```
 * and a result of test step execution is a [Map], having Json String from grouped by different services they came from.
 * A json response loos like this:
 *```
 *  {
 *     "items" [
 *       {
 *           "accounts" [
 *              {
 *                  "name": "Acc A"
 *                  "email": "accA@mail.com"
 *              }
 *           ]
 *       }
 *     ]
 *  }
 *```
 *
 * If you apply that [Map] result to the [ValidationNode] constructed based on the validation details above, then it will do the following:
 * - Gets value from key 'service_1' of the [Map]
 * - Takes the previous value and applies the function [jsonToMap()][org.skellig.teststep.processing.value.function.JsonToMapTestStepFunctionExecutor]' which converts
 *   the Json String to [Map]
 * - Takes the previous value of [Map] and gets the value from key 'items' and then from index 0 which is another [Map]
 * - Takes the previous value of [Map] and gets the value from 'accounts', then from index 0 by calling function [fromIndex()][org.skellig.teststep.processing.value.function.FromIndexFunctionExecutor]
 *  which results in another [Map]
 * - Takes the previous value of [Map] and gets the value from 'name' then verifies that it equals to "Acc A". Does the same for 'email' property.
 *
 * @property valueExpressionContextFactory The [ValueExpressionContextFactory] for creating value expression contexts for evaluation of [ValueExpression]
 */
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