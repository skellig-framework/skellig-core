package org.skellig.teststep.reader.value.expression

import java.math.BigDecimal

class MathOperationExpression(private val operator: String,
                              private val leftExpression: ValueExpression,
                              private val rightExpression: ValueExpression
) : ValueExpression {

    //TODO: handle nullable evaluated values
    override fun evaluate(context: ValueExpressionContext): Any {
        var evaluatedLeft = leftExpression.evaluate(context)
        var evaluatedRight = rightExpression.evaluate(context)
        return if (operator == "+" && (evaluatedLeft is String || evaluatedRight is String)) {
            evaluatedLeft.toString() + evaluatedRight
        } else {
            if (!(evaluatedLeft is BigDecimal && evaluatedRight is BigDecimal)) {
                evaluatedLeft = evaluatedLeft.toString().toBigDecimal()
                evaluatedRight = evaluatedRight.toString().toBigDecimal()
            }
            when (operator) {
                "+" -> evaluatedLeft.plus(evaluatedRight)
                "-" -> evaluatedLeft.minus(evaluatedRight)
                "*" -> evaluatedLeft.times(evaluatedRight)
                "/" -> evaluatedLeft.div(evaluatedRight)
                else -> throw IllegalArgumentException("Invalid math operator: \$operator")
            }
        }
    }

    override fun toString(): String {
        return "$leftExpression $operator $rightExpression"
    }

    override fun equals(other: Any?): Boolean {
        return if (other is MathOperationExpression)
            operator == other.operator &&
                    leftExpression == other.leftExpression &&
                    rightExpression == other.rightExpression
        else false
    }

    override fun hashCode(): Int {
        return operator.hashCode() + leftExpression.hashCode() + rightExpression.hashCode()
    }
}