package org.skellig.teststep.reader.value.expression

class MapValueExpression(val value: Map<ValueExpression, ValueExpression?>) : ValueExpression {
    override fun evaluate(context: ValueExpressionContext): Any {
        return value.map { it.key.evaluate(context) to it.value?.evaluate(context) }.toMap()
    }

    override fun toString(): String {
        return value.toString()
    }
}