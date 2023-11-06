package org.skellig.teststep.reader.sts.value.expression

class PropertyValueExpression(private val key: String, private val defaultValue: ValueExpression) : ValueExpression {

    override fun evaluate(context: ValueExpressionContext): Any? {
        return context.propertyCallDelegate(key) { defaultValue.evaluate(context) }
    }

    override fun toString(): String {
        return "\${$key: $defaultValue}"
    }
}