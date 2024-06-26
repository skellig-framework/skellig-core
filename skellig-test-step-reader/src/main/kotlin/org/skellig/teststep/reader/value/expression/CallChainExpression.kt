package org.skellig.teststep.reader.value.expression

/**
 * Represents a call chain expression, which is a sequence of value expressions separated by dots,
 * indicating a chain of method calls or property access operations, for example:
 * ```
 * ${keyA}.prices."book.name"
 * ```
 *
 * @param callChain The list of value expressions in the call chain.
 */
class CallChainExpression(val callChain: List<ValueExpression?>) : ValueExpression {

    override fun evaluate(context: ValueExpressionContext): Any? {
        var result: Any? = context.value
        result?.let { context.evaluationType = EvaluationType.CALL_CHAIN }
        (callChain.indices step 1)
            .forEach {
                if (it > 0 && result == null)
                    throw NullPointerException("Failed to call '${callChain[it]}' on null value")

                context.value = result
                if(it > 0) context.evaluationType = EvaluationType.CALL_CHAIN
                result = callChain[it]?.evaluate(context)
                context.evaluationType = EvaluationType.DEFAULT
            }
        context.value = null
        return result
    }

    override fun toString(): String {
        return callChain.joinToString(".")
    }

    override fun equals(other: Any?): Boolean {
        return if (other is CallChainExpression) callChain == other.callChain
        else false
    }

    override fun hashCode(): Int {
        return callChain.sumOf { it.hashCode() }
    }
}