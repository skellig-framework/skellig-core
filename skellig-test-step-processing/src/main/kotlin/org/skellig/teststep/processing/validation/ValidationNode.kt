package org.skellig.teststep.processing.validation

import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.reader.sts.value.expression.ValueExpression

interface ValidationNode {
    fun validate(value: Any?)
}

class SingleValidationNode(
    val expected: ValueExpression?,
    private val parameters: Map<String, Any?>,
    private val valueExpressionContextFactory: ValueExpressionContextFactory
) : ValidationNode {

    override fun validate(value: Any?) {
        val evaluated = expected?.evaluate(valueExpressionContextFactory.createForValidation(value, parameters))

        if (evaluated is Boolean) {
            if (!evaluated)
                throw ValidationException(
                    "Validation failed for '$expected'!\n" +
                            "Actual: $value\n" +
                            "Expected: $evaluated"
                )
        } else {
            if (evaluated != value) {
                throw ValidationException(
                    "Validation failed!\n" +
                            "Actual: $value\n" +
                            "Expected: $evaluated"
                )
            }
        }
    }
}

class RootValidationNodes(val nodes: List<ValidationNode>) : ValidationNode {

    override fun validate(value: Any?) {
        nodes.forEach { it.validate(value) }
    }
}

class ValidationNodes(val nodes: List<ValidationNode>) : ValidationNode {

    override fun validate(value: Any?) {
        if (value is List<*>) {
            nodes.forEach { n ->
                var errors = ""
                if (!value.any { v ->
                        try {
                            n.validate(v)
                            true
                        } catch (ex: ValidationException) {
                            errors += "${ex.message}\n"
                            false
                        }
                    }) throw ValidationException(errors)
            }
        } else {
            nodes.forEach { it.validate(value) }
        }
    }
}

class PairValidationNode(
    val actual: ValueExpression,
    val expected: ValueExpression?,
    private val parameters: Map<String, Any?>,
    private val valueExpressionContextFactory: ValueExpressionContextFactory
) : ValidationNode {

    override fun validate(value: Any?) {
        val contextForActual = valueExpressionContextFactory.createForValidation(value, parameters)
        val contextForExpected = valueExpressionContextFactory.createForValidation(parameters, value)

        val actualEvaluated = actual.evaluate(contextForActual)
        val expectedEvaluated = expected?.evaluate(contextForExpected)
        if (actualEvaluated != expectedEvaluated)
            throw ValidationException(
                "Validation failed for '$actual = $expected'!\n" +
                        "Actual: $actualEvaluated\n" +
                        "Expected: $expectedEvaluated"
            )
    }
}

class GroupedValidationNode(
    val actual: ValueExpression,
    val items: ValidationNode,
    private val parameters: Map<String, Any?>,
    private val valueExpressionContextFactory: ValueExpressionContextFactory
) : ValidationNode {

    override fun validate(value: Any?) {
        val evaluatedActualValue = actual.evaluate(valueExpressionContextFactory.createForValidation(value, parameters))
        items.validate(evaluatedActualValue)
    }
}