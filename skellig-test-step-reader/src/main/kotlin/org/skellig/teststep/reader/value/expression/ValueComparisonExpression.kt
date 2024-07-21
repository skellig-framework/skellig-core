package org.skellig.teststep.reader.value.expression

import java.math.BigDecimal
import java.math.BigInteger

/**
 * Represents a value comparison expression that compares two value expressions using a specified operator
 *
 * @property operator The comparison operator (==, !=, >, >=, <, <=)
 * @property leftExpression The left value expression to compare.
 * @property rightExpression The right value expression to compare.
 */
class ValueComparisonExpression(
    private val operator: String,
    private val leftExpression: ValueExpression,
    private val rightExpression: ValueExpression
) : ValueExpression {

    companion object {
        fun compare(operator: String, left: Any?, right: Any?): Boolean {
            val evaluatedLeft = convertToBigDecimal(left)
            val evaluatedRight = convertToBigDecimal(right)
            return when (operator) {
                ">" -> evaluatedLeft > evaluatedRight
                ">=" -> evaluatedLeft >= evaluatedRight
                "<" -> evaluatedLeft < evaluatedRight
                "<=" -> evaluatedLeft <= evaluatedRight
                "==" -> evaluatedLeft == evaluatedRight
                "!=" -> evaluatedLeft != evaluatedRight
                else -> throw IllegalArgumentException("Invalid comparison operator for numeric values: $operator")
            }
        }

        private fun convertToBigDecimal(value: Any?): BigDecimal {
            return when (value) {
                is BigDecimal -> value
                is Int -> BigDecimal(value)
                is Long -> BigDecimal(value)
                is Double -> BigDecimal(value)
                is Float -> BigDecimal(value.toDouble())
                is BigInteger -> BigDecimal(value)
                else -> BigDecimal(value.toString())
            }
        }
    }

    override fun evaluate(context: ValueExpressionContext): Any {
        val evaluatedLeft = leftExpression.evaluate(context)
        val evaluatedRight = rightExpression.evaluate(context)
        return if (evaluatedLeft is Number || evaluatedRight is Number) {
            compare(operator, evaluatedLeft, evaluatedRight)
        } else {
            when (operator) {
                "==" -> evaluatedLeft == evaluatedRight
                "!=" -> evaluatedLeft != evaluatedRight
                else -> throw IllegalArgumentException("Invalid comparison operator for String values: $operator")
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