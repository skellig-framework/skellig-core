package org.skellig.teststep.reader.sts.value.expression

class FunctionCallExpression(private val name: String, private val args: List<ValueExpression?>) : ValueExpression {

    override fun evaluate(context: ValueExpressionContext): Any? {
        // if value from context is not null, then the function should be called as a method of this value
        return context.functionCallDelegate(name, context.value, args.map { it?.evaluate(context) }.toTypedArray())
    }

    override fun toString(): String {
        return if (args.isEmpty()) "$name()" else "$name(${args.joinToString(",")})"
    }

    override fun equals(other: Any?): Boolean {
        return if (other is FunctionCallExpression)
            name == other.name &&
                    args == other.args
        else false
    }

    override fun hashCode(): Int {
        return name.hashCode() + args.hashCode()
    }
}