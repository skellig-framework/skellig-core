package org.skellig.teststep.reader.value.expression


/**
 * Represents a value expression that retrieves a property value from.
 * The default syntax of the Property expression is `${property_key, default value}`
 *
 * @property key The key used to retrieve the property.
 * @property defaultValue The default value to use if value of the property not found.
 */
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