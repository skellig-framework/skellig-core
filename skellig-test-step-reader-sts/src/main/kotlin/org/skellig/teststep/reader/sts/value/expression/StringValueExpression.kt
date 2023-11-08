package org.skellig.teststep.reader.sts.value.expression

class StringValueExpression(private val value: String) : ValueExpression {
    override fun evaluate(context: ValueExpressionContext): Any? {
        return if (context.evaluationType == EvaluationType.CALL_CHAIN) context.functionCallDelegate(value, context.value, emptyArray())
        else value
    }
    override fun toString(): String {
        return value
    }
}