package org.skellig.teststep.reader.sts.value.expression

import java.math.BigDecimal

class MathOperationExpression(private val operator: String, private val left: ValueExpression, private val right: ValueExpression) : ValueExpression {

    override fun evaluate(context: ValueExpressionContext): Any {
        var evaluatedLeft = left.evaluate(context)
        var evaluatedRight = right.evaluate(context)
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
        return "$left $operator $right"
    }
}