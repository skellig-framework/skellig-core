package org.skellig.teststep.reader.value.expression

class PropertyValueExpression(private val key: String, private val defaultValue: ValueExpression? = null) : ValueExpression {

    override fun evaluate(context: ValueExpressionContext): Any? {
        return context.onGetReferenceValue(key) { defaultValue?.evaluate(context) }
    }

    override fun toString(): String {
        return defaultValue?.let { "\${$key, $defaultValue}" } ?: "\${$key}"
    }

    override fun equals(other: Any?): Boolean {
        return if (other is PropertyValueExpression) key == other.key
        else false
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }
}