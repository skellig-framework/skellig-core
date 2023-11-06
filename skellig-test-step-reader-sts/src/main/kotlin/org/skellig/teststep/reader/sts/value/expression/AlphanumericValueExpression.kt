package org.skellig.teststep.reader.sts.value.expression

class AlphanumericValueExpression(private val value: String) : ValueExpression {
    override fun evaluate(context: ValueExpressionContext): Any? {
        return if (context.hasLambdaParameterWithName(value)) context.getLambdaExpressionParameter(value)
        else value
    }

    override fun toString(): String {
        return value
    }
}