package org.skellig.teststep.reader.value.expression

class ListValueExpression(val value: List<ValueExpression?>) : ValueExpression {

    override fun evaluate(context: ValueExpressionContext): Any {
        return value.map { it?.evaluate(context) }.toList()
    }

    override fun toString(): String {
        return value.toString()
    }
}