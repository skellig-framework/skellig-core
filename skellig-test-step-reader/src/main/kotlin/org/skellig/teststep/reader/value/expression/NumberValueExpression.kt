package org.skellig.teststep.reader.value.expression

import java.math.BigDecimal


/**
 * Represents a value expression that holds a numeric value.
 * Type of the numeric value is [BigDecimal].
 *
 * @property value The numeric value of the expression.
 */
class NumberValueExpression(value: String?) : ValueExpression {

    private val value: BigDecimal = BigDecimal(value)

    override fun evaluate(context: ValueExpressionContext): Any {
        return value
    }

    override fun toString(): String {
        return value.toString()
    }

    override fun equals(other: Any?): Boolean {
        return if (other is NumberValueExpression) value == other.value
        else false
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}