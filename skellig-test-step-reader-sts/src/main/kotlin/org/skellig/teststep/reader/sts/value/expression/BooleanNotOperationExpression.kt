package org.skellig.teststep.reader.sts.value.expression

class BooleanNotOperationExpression(private val valueExpression: ValueExpression) : ValueExpression {

    override fun evaluate(context: ValueExpressionContext): Any {
        return !(valueExpression.evaluate(context) as Boolean)
    }
}