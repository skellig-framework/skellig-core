package org.skellig.teststep.reader.sts.value.expression

import java.math.BigDecimal
import java.math.RoundingMode

class MathOperationExpression(private val operator: String, private val left: ValueExpression, private val right: ValueExpression) : ValueExpression {

    override fun evaluate(): Any? {
        val result: BigDecimal
        val evaluatedLeft = left.evaluate() as BigDecimal
        val evaluatedRight = right.evaluate() as BigDecimal
        result = when (operator) {
            "+" -> evaluatedLeft.add(evaluatedRight)
            "-" -> evaluatedLeft.subtract(evaluatedRight)
            "*" -> evaluatedLeft.multiply(evaluatedRight)
            "/" -> evaluatedLeft.divide(evaluatedRight, RoundingMode.CEILING)
            else -> throw IllegalArgumentException("Invalid operator: \$operator")
        }
        return result
    }

    override fun toString(): String {
        return "$left $operator $right"
    }
}