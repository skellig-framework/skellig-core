package org.skellig.teststep.reader.sts.value.expression

class BooleanOperationExpression(private val operator: String,
                                 private val left: ValueExpression,
                                 private val right: ValueExpression) : ValueExpression {

    override fun evaluate(): Any? {
        return false
    }
}