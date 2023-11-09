package org.skellig.teststep.reader.sts.value.expression

class BooleanNotOperationExpression(private val valueExpression: ValueExpression?) : ValueExpression {

    override fun evaluate(context: ValueExpressionContext): Any {
        val result = valueExpression?.evaluate(context) ?: throw IllegalArgumentException("Cannot apply NOT operation on null")
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