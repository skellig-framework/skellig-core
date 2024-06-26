package org.skellig.teststep.reader.value.expression

/**
 * Represents a boolean operation expression.
 *
 * @property operator The operator of the boolean operation expression (&&, ||)
 * @property leftExpression The left value expression.
 * @property rightExpression The right value expression.
 */
class BooleanOperationExpression(
    private val operator: String,
    private val leftExpression: ValueExpression,
    private val rightExpression: ValueExpression
) : ValueExpression {

    override fun evaluate(context: ValueExpressionContext): Any {
        val evaluatedLeft = leftExpression.evaluate(context)
        val evaluatedRight = rightExpression.evaluate(context)

        if (evaluatedLeft is Boolean && evaluatedRight is Boolean) {
            return when (operator) {
                "&&" -> evaluatedLeft && evaluatedRight
                "||" -> evaluatedLeft || evaluatedRight
                else -> throw IllegalArgumentException("Invalid boolean operator: \$operator")
            }
        } else throw IllegalArgumentException("Failed to evaluate boolean operator '$operator' on non-boolean values '$evaluatedLeft' and '$evaluatedRight'")
    }

    override fun equals(other: Any?): Boolean {
        return if (other is BooleanOperationExpression)
            operator == other.operator &&
                    leftExpression == other.leftExpression &&
                    rightExpression == other.rightExpression
        else false
    }

    override fun hashCode(): Int {
        return operator.hashCode() + leftExpression.hashCode() + rightExpression.hashCode()
    }

    override fun toString(): String {
        return "$leftExpression $operator $rightExpression"
    }


}