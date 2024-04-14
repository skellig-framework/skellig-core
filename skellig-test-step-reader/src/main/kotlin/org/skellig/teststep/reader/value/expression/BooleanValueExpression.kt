package org.skellig.teststep.reader.value.expression


/**
 * Represents a boolean value expression (true or false).
 *
 * @property value The boolean value of the expression.
 */
class BooleanValueExpression(value: String) : ValueExpression {
    private val value: Boolean = value.toBoolean()

    override fun evaluate(context: ValueExpressionContext): Any = value

    override fun toString(): String {
        return value.toString()
    }

    override fun equals(other: Any?): Boolean {
        return if (other is BooleanValueExpression) value == other.value
        else false
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}