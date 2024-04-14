package org.skellig.teststep.reader.value.expression

/**
 * The `MapValueExpression` is a wrapper class representing a Map value of a property.
 * Also, this wrapper can be used as an argument in a [FunctionCallExpression].
 *
 * @property value The map of `ValueExpression` key-value pairs.
 */
class MapValueExpression(val value: Map<ValueExpression, ValueExpression?>) : ValueExpression {
    override fun evaluate(context: ValueExpressionContext): Any {
        return value.map { it.key.evaluate(context) to it.value?.evaluate(context) }.toMap()
    }

    override fun toString(): String {
        return value.toString()
    }
}