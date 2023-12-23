package org.skellig.teststep.reader.value.expression

class FunctionCallExpression(val name: String, val args: Array<ValueExpression?>) : ValueExpression {

    override fun evaluate(context: ValueExpressionContext): Any? {
        context.evaluationType = EvaluationType.DEFAULT
        // if value from context is not null, then the function should be called as a method of this value
        return context.onFunctionCall(name, context.value, args.map { it?.evaluate(context) }.toTypedArray())
    }

    override fun toString(): String {
        return if (args.isEmpty()) "$name()" else "$name(${args.joinToString(",")})"
    }

    override fun equals(other: Any?): Boolean {
        return if (other is FunctionCallExpression)
            name == other.name &&
                    args.contentEquals(other.args)
        else false
    }

    override fun hashCode(): Int {
        return name.hashCode() + args.hashCode()
    }
}