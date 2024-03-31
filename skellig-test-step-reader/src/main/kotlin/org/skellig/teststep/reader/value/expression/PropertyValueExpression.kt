package org.skellig.teststep.reader.value.expression

class PropertyValueExpression(private val key: ValueExpression, private val defaultValue: ValueExpression? = null) : ValueExpression {

    constructor(key: String, defaultValue: ValueExpression? = null)
            : this(AlphanumericValueExpression(key), defaultValue)

    override fun evaluate(context: ValueExpressionContext): Any? {
        return key.evaluate(context)?.toString()?.let { context.onGetReferenceValue(it) { defaultValue?.evaluate(context) } }
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