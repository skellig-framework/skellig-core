package org.skellig.teststep.processing.model

import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.util.PropertyFormatUtils.Companion.createIndent
import org.skellig.teststep.processing.util.debug
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.reader.value.expression.*

/**
 * The ValidationNode interface represents a node in a validation tree. This interface provides a method to validate a given value.
 *
 * @see ValidationNode.validate
 */
interface ValidationNode {

    /**
     * Validates a given value. If not valid, then throws [ValidationException].
     *
     * @param value The value to be validated.
     */
    fun validate(value: Any?)
}

internal abstract class BaseValidationNode : ValidationNode {

    protected val log = logger<ValidationNode>()

    override fun toString(): String {
        return toString(0)
    }

    abstract fun toString(indent: Int): String

    protected fun createContext(
        valueExpression: ValueExpression?,
        valueExpressionContextFactory: ValueExpressionContextFactory,
        value: Any?,
        parameters: Map<String, Any?>
    ): ValueExpressionContext {
        return if (valueExpression?.javaClass == AlphanumericValueExpression::class.java ||
            valueExpression?.javaClass == CallChainExpression::class.java ||
            valueExpression?.javaClass == FunctionCallExpression::class.java
        ) {
            valueExpressionContextFactory.createForValidationAsCallChain(value, parameters)
        } else valueExpressionContextFactory.createForValidation(value, parameters, true)
    }

    protected fun createContextForExpectedValue(
        valueExpressionContextFactory: ValueExpressionContextFactory,
        value: Any?,
        parameters: Map<String, Any?>
    ): ValueExpressionContext {
        return valueExpressionContextFactory.createForValidation(value, parameters, false)
    }
}


/**
 * The ValidationNodes class represents a collection of [ValidationNode] instances.
 * This class is usually used as value of [GroupedValidationNode] for the property [GroupedValidationNode.items].
 *
 * @property nodes The list of [ValidationNode] instances.
 * @property isMatchPerItem A flag indicating whether each item in a collection should be matched individually.
 * The value 'true' is used whet it needs to verify all [ValidationNode]s inside a [list][ListValueExpression]. If at least
 * one item in the list is not valid, then it throws [ValidationException].
 *
 * The value 'false' is used by default, and it validates the whole [ValidationNode] as a single unit.
 *
 * @see org.skellig.teststep.processing.model.factory.ValidationNodeFactory for more details
 */
internal open class ValidationNodes(
    val nodes: List<ValidationNode>,
    private val isMatchPerItem: Boolean = false
) : BaseValidationNode() {

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
        return nodes.joinToString("\n", "${createIndent(indent)}{\n", "\n${createIndent(indent)}}\n", transform = { n -> (n as BaseValidationNode).toString(indent + 1) })
    }
}

/**
 * Represents a grouped validation node.
 *
 * @property actual The actual value expression.
 * @property items The validation node applied to the value from [actual].
 * @property parameters The parameters for the validation node used in evaluation of [ValueExpression].
 * @property valueExpressionContextFactory The factory for creating value expression contexts used in evaluation of [ValueExpression].
 */
internal class GroupedValidationNode(
    val actual: ValueExpression,
    val items: BaseValidationNode,
    private val parameters: Map<String, Any?>,
    private val valueExpressionContextFactory: ValueExpressionContextFactory
) : BaseValidationNode() {

    override fun validate(value: Any?) {
        val evaluatedActualValue = actual.evaluate(createContext(actual, valueExpressionContextFactory, value, parameters))
        log.debug { "Start to verify '$evaluatedActualValue' evaluated from '$actual'\n" }
        items.validate(evaluatedActualValue)
    }

    override fun toString(indent: Int): String {
        return "${createIndent(indent)}$actual: ${items.toString(indent)}"
    }
}

/**
 * Represents a validation node that compares the actual and expected values using the given expressions.
 * This node compares the expected value with the actual value and throws a [ValidationException] if they do not match.
 *
 * @param actual The expression representing the actual value.
 * @param expected The expression representing the expected value or null.
 * @param parameters The parameters for the validation node used in evaluation of [ValueExpression].
 * @param valueExpressionContextFactory The factory for creating value expression contexts used in evaluation of [ValueExpression].
 */
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
                        "Expected: $expectedEvaluated\n" +
                        "Actual: $actualEvaluated"
            )
        else log.debug {
            "Verify that '$actual' is '$expected'\n" +
                    "Expected: $expectedEvaluated\n" +
                    "Actual: $actualEvaluated\n"
        }
    }

    override fun toString(indent: Int): String {
        return "${createIndent(indent)}$actual = $expected"
    }
}

/**
 * Represents a single validation node used in the validation process.
 * This node compares the expected value with the actual value provided when calling [validate] method
 * and throws a [ValidationException] if they do not match. If the [expected] value is evaluated to Boolean (true or false),
 * then it throws [ValidationException] if the value is 'false'.
 *
 * @property expected The expected value expression.
 * @param parameters The parameters for the validation node used in evaluation of [ValueExpression].
 * @param valueExpressionContextFactory The factory for creating value expression contexts used in evaluation of [ValueExpression].
 */
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
                            "Expected: $evaluated" +
                            "Actual: false"
                )
            else log.debug {
                "Verify that '$value' is '$expected'\n" +
                        "Expected: $evaluated\n" +
                        "Actual: true\n"
            }
        } else {
            if (evaluated != value) {
                throw ValidationException(
                    "Validation failed!\n" +
                            "Expected: $evaluated\n" +
                            "Actual: $value"
                )
            } else log.debug {
                "Verify that '$value' is '$expected'\n" +
                        "Expected: $evaluated\n" +
                        "Actual: $value\n"
            }
        }
    }

    override fun toString(indent: Int): String {
        return "${createIndent(indent)}${expected?.toString() ?: "null"}"
    }
}
