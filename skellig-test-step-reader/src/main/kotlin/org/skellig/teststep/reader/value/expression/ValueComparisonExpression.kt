package org.skellig.teststep.reader.value.expression

import java.math.BigDecimal

class ValueComparisonExpression(
    private val operator: String,
    private val leftExpression: ValueExpression,
    private val rightExpression: ValueExpression
) : ValueExpression {

    override fun evaluate(context: ValueExpressionContext): Any {
        var evaluatedLeft = leftExpression.evaluate(context)
        var evaluatedRight = rightExpression.evaluate(context)
        return if (evaluatedLeft is String || evaluatedRight is String) {
            when (operator) {
                "==" -> evaluatedLeft.toString() == evaluatedRight.toString()
                "!=" -> evaluatedLeft.toString() != evaluatedRight.toString()
                else -> throw IllegalArgumentException("Invalid comparison operator for String values: $operator")
            }
        } else {
            if (!(evaluatedLeft is BigDecimal && evaluatedRight is BigDecimal)) {
                evaluatedLeft = evaluatedLeft.toString().toBigDecimal()
                evaluatedRight = evaluatedRight.toString().toBigDecimal()
            }
            when (operator) {
                ">" -> evaluatedLeft > evaluatedRight
                ">=" -> evaluatedLeft >= evaluatedRight
                "<" -> evaluatedLeft < evaluatedRight
                "<=" -> evaluatedLeft <= evaluatedRight
                "==" -> evaluatedLeft == evaluatedRight
                "!=" -> evaluatedLeft != evaluatedRight
                else -> throw IllegalArgumentException("Invalid comparison operator for numeric values: $operator")
            }
        }
    }

    override fun toString(): String {
        return "$leftExpression $operator $rightExpression"
    }

    override fun equals(other: Any?): Boolean {
        return if (other is ValueComparisonExpression)
            operator == other.operator &&
                    leftExpression == other.leftExpression &&
                    rightExpression == other.rightExpression
        else false
    }

    override fun hashCode(): Int {
        return operator.hashCode() + leftExpression.hashCode() + rightExpression.hashCode()
    }
}