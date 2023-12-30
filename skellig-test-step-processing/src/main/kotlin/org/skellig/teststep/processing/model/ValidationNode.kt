package org.skellig.teststep.processing.model

import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.reader.value.expression.*

interface ValidationNode {
    fun validate(value: Any?)
}

internal abstract class BaseValidationNode : ValidationNode {
    abstract fun toString(indent: Int): String

    protected fun createIndent(indent: Int): String = "\t".repeat(indent)

    protected fun createContext(
        valueExpression: ValueExpression?,
        valueExpressionContextFactory: ValueExpressionContextFactory,
        value: Any?,
        parameters: Map<String, Any?>
    ): ValueExpressionContext {
        return if (valueExpression?.javaClass == AlphanumericValueExpression::class.java ||
            valueExpression?.javaClass == CallChainExpression::class.java ||
            valueExpression?.javaClass == FunctionCallExpression::class.java)
            valueExpressionContextFactory.createForValidationAsCallChain(value, parameters)
        else valueExpressionContextFactory.createForValidation(value, parameters, true)
    }

    protected fun createContextForExpectedValue(
        valueExpressionContextFactory: ValueExpressionContextFactory,
        value: Any?,
        parameters: Map<String, Any?>
    ): ValueExpressionContext {
        return valueExpressionContextFactory.createForValidation(value, parameters, false)
    }
}

internal open class ValidationNodes(val nodes: List<ValidationNode>,
                                    private val isMatchPerItem: Boolean = false) : BaseValidationNode() {

    override fun validate(value: Any?) {
        if (isMatchPerItem && value is Collection<*>) {
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
        } else nodes.forEach { it.validate(value) }
    }

    override fun toString(indent: Int): String {
        return nodes.joinToString("\n", "${createIndent(indent)}[", "${createIndent(indent)}]", transform = { n -> (n as BaseValidationNode).toString(indent + 1) })
    }

}

internal class GroupedValidationNode(
    val actual: ValueExpression,
    val items: BaseValidationNode,
    private val parameters: Map<String, Any?>,
    private val valueExpressionContextFactory: ValueExpressionContextFactory
) : BaseValidationNode() {

    override fun validate(value: Any?) {
        val evaluatedActualValue = actual.evaluate(createContext(actual, valueExpressionContextFactory, value, parameters))
        items.validate(evaluatedActualValue)
    }

    override fun toString(indent: Int): String {
        return "\n${createIndent(indent)}$actual: ${items.toString(indent + 1)}"
    }
}

internal class PairValidationNode(
    val actual: ValueExpression,
    val expected: ValueExpression?,
    private val parameters: Map<String, Any?>,
    private val valueExpressionContextFactory: ValueExpressionContextFactory
) : BaseValidationNode() {

    override fun validate(value: Any?) {
        val contextForActual = createContext(actual, valueExpressionContextFactory, value, parameters)
        val contextForExpected = createContextForExpectedValue(valueExpressionContextFactory, value, parameters)

        val actualEvaluated = actual.evaluate(contextForActual)
        val expectedEvaluated = expected?.evaluate(contextForExpected)
        if (actualEvaluated != expectedEvaluated)
            throw ValidationException(
                "Validation failed for '$actual = $expected'!\n" +
                        "Actual: $actualEvaluated\n" +
                        "Expected: $expectedEvaluated"
            )
    }

    override fun toString(indent: Int): String {
        return "${createIndent(indent)}$actual = $expected"
    }
}

internal class SingleValidationNode(
    val expected: ValueExpression?,
    private val parameters: Map<String, Any?>,
    private val valueExpressionContextFactory: ValueExpressionContextFactory
) : BaseValidationNode() {

    override fun validate(value: Any?) {
        val evaluated = expected?.evaluate(createContextForExpectedValue(valueExpressionContextFactory, value, parameters))

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

    override fun toString(indent: Int): String {
        return expected?.toString() ?: "null"
    }
}
