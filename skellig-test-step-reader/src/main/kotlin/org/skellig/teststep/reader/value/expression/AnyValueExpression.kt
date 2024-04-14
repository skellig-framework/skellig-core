package org.skellig.teststep.reader.value.expression


/**
 * Represents an expression that holds a specific value.
 *
 * @property value The value of the expression.
 */
class AnyValueExpression(private val value: Any) : ValueExpression {
   
    override fun evaluate(context: ValueExpressionContext): Any {
        return value
    }

}