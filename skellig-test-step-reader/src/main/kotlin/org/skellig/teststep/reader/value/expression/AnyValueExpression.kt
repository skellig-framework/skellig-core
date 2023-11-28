package org.skellig.teststep.reader.value.expression

class AnyValueExpression(private val value: Any) : ValueExpression {
   
    override fun evaluate(context: ValueExpressionContext): Any {
        return value
    }

}