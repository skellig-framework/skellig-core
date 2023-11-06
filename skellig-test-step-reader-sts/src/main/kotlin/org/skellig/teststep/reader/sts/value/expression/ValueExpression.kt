package org.skellig.teststep.reader.sts.value.expression

interface ValueExpression {
    fun evaluate(context: ValueExpressionContext): Any?
}