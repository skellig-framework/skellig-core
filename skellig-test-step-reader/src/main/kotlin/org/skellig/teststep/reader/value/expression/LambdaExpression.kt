package org.skellig.teststep.reader.value.expression

class LambdaExpression(private val name: String, private val body: ValueExpression) : ValueExpression {

    /*
    * Lambda expressions are normally evaluated in a loop by a dedicated function (ex. any, map, etc.),
    * thus it should return a consumer with lazy execution of evaluate method.
    * */
    override fun evaluate(context: ValueExpressionContext): Any {
        return { item : Any? ->
            context.setLambdaExpressionParameter(name, item)
            body.evaluate(ValueExpressionContext(context))
        }
    }

    override fun toString(): String {
        return "$name -> $body"
    }
}