package org.skellig.teststep.reader.value.expression

/**
 * Represents a boolean NOT operation (!) expression, for example:
 * ```
 * !(${a} > ${b})
 * ```
 *
 * @property valueExpression The value expression to be negated.
 */
class BooleanNotOperationExpression(private val valueExpression: ValueExpression?) : ValueExpression {

    override fun evaluate(context: ValueExpressionContext): Any {
        val result = valueExpression?.evaluate(context) ?: throw IllegalArgumentException("Cannot apply NOT operation (!) on null")
        return if (result is Boolean) !result
        else throw IllegalArgumentException("Expected boolean result but ${result.javaClass} evaluated for NOT operation of '$valueExpression'")
    }

    override fun equals(other: Any?): Boolean {
        return if (other is BooleanNotOperationExpression) valueExpression == other.valueExpression
        else false
    }

    override fun hashCode(): Int {
        return valueExpression?.hashCode() ?: 0
    }
}