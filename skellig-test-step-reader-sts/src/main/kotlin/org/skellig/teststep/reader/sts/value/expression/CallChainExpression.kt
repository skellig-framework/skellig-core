package org.skellig.teststep.reader.sts.value.expression

class CallChainExpression(private val callChain: List<ValueExpression?>) : ValueExpression {

    override fun evaluate(context: ValueExpressionContext): Any? {
        var result: Any? = null
        context.evaluationType = EvaluationType.CALL_CHAIN
        (callChain.indices step 1)
            .forEach {
                if (it > 0 && result == null)
                    throw NullPointerException("Failed to call '${callChain[it]}' on null value")

                context.value = result
                result = callChain[it]?.evaluate(context)
            }
        context.evaluationType = EvaluationType.DEFAULT
        context.value = null
        return result
    }

    override fun toString(): String {
        return callChain.joinToString(".")
    }
}