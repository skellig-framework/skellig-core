package org.skellig.teststep.reader.value.expression

import java.math.BigDecimal

class NumberValueExpression(value: String?) : ValueExpression {

    private val value: BigDecimal

    init {
        this.value = BigDecimal(value)
    }

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