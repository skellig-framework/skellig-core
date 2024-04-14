package org.skellig.teststep.reader.value.expression


/**
 * The `ListValueExpression` is a wrapper class representing a List value of a property.
 * Also, this wrapper can be used as an argument in a [FunctionCallExpression].*
 *
 * @property value The list of value expressions.
 */
class ListValueExpression(val value: List<ValueExpression?>) : ValueExpression {

    override fun evaluate(context: ValueExpressionContext): Any {
        return value.map { it?.evaluate(context) }.toList()
    }

    override fun toString(): String {
        return value.toString()
    }
}