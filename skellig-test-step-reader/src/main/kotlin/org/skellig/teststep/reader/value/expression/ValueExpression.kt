package org.skellig.teststep.reader.value.expression

/**
 * Represents an expression that can be evaluated to a value.
 */
interface ValueExpression {

    /**
     * Evaluates the value expression in the given context.
     *
     * @param context The value expression context.
     * @return The result of evaluating the expression.
     */
    fun evaluate(context: ValueExpressionContext): Any?
}