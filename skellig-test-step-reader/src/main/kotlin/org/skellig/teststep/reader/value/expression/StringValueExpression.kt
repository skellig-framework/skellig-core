package org.skellig.teststep.reader.value.expression

/**
 * Represents a string value expression that can be evaluated to a string value.
 *
 * @property value The string value of the expression.
 */
class StringValueExpression(private val value: String) : ValueExpression {
    override fun evaluate(context: ValueExpressionContext): Any? {
        return if (context.evaluationType == EvaluationType.CALL_CHAIN) context.onFunctionCall(value, context.value, emptyArray())
        else value
    }

    override fun toString(): String {
        return value
    }

    override fun equals(other: Any?): Boolean {
        return if (other is StringValueExpression) value == other.value
        else value == other
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}