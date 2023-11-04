package org.skellig.teststep.reader.sts.value.expression

import java.math.BigDecimal

class NumberValueExpression(value: String?) : ValueExpression {

    private val value: BigDecimal

    init {
        this.value = BigDecimal(value)
    }

    override fun evaluate(): Any {
        return value
    }

    override fun toString(): String {
        return value.toString()
    }
}