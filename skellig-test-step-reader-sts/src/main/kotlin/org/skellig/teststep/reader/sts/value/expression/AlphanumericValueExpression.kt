package org.skellig.teststep.reader.sts.value.expression

class AlphanumericValueExpression(private val value: String) : ValueExpression {

    override fun evaluate(context: ValueExpressionContext): Any? {
        return if (context.hasLambdaParameterWithName(value)) context.getLambdaExpressionParameter(value)
        else if (context.evaluationType == EvaluationType.CALL_CHAIN) context.functionCallDelegate(value, context.value, emptyArray())
        else value
    }

    override fun toString(): String {
        return value
    }

    override fun equals(other: Any?): Boolean {
        return if (other is AlphanumericValueExpression) value == other.value
        else false
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}