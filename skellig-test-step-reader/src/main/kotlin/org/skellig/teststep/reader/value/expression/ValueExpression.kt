package org.skellig.teststep.reader.value.expression

interface ValueExpression {
    fun evaluate(context: ValueExpressionContext): Any?
}