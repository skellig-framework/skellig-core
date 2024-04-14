package org.skellig.teststep.reader.value.expression


/**
 * Represents a Lambda Expression.
 * Lambda expressions are normally evaluated in a loop by a dedicated function (ex. any, map, etc.),
 * thus it should return a consumer with lazy execution of evaluated method.
 *
 * As an example, a lambda expression can look like this (as a function argument):
 * ```
 * funcA(x -> x + 1)
 * ```
 * and in order to reference value x, you can simply use it as is in the lambda expression body as deep as needed:
 * ```
 * funcA(x -> x.sum(i -> x.delta * i.price))
 * ```
 *
 * Only 1 parameter is supported in [LambdaExpression] which is set in the property [name].
 *
 * @param name The name of parameter in the lambda expression.
 * @param body The body of the lambda expression.
 */
class LambdaExpression(private val name: String, private val body: ValueExpression) : ValueExpression {

    override fun evaluate(context: ValueExpressionContext): Any {
        return { item: Any? ->
            context.setLambdaExpressionParameter(name, item)
            body.evaluate(ValueExpressionContext(context))
        }
    }

    override fun toString(): String {
        return "$name -> $body"
    }
}