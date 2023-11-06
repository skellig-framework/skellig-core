package org.skellig.teststep.reader.sts.value.expression

class FunctionCallExpression(private val name: String, private val args: List<ValueExpression?>) : ValueExpression {

    override fun evaluate(context: ValueExpressionContext): Any? {
        return context.functionCallDelegate(name, args.map { it?.evaluate(context) }.toTypedArray())
    }

    override fun toString(): String {
        return "$name($args)"
    }
}