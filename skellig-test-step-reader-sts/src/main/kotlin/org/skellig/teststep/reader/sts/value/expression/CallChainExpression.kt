package org.skellig.teststep.reader.sts.value.expression

class CallChainExpression(private val callChain: List<ValueExpression?>) : ValueExpression {

    override fun evaluate(context: ValueExpressionContext): Any? {
        var result: Any? = null
        if (callChain.isNotEmpty()) {
            var startFrom = 0
            val firstInChain = callChain[0]
            // check if first in chain is a parameter from lambda expression which can be assigned to the result.
            // Otherwise, just continue normally.
            if (firstInChain is StringValueExpression) {
                val text = firstInChain.evaluate(context).toString()
                if(context.lambdaExpressionParameters.containsKey(text)) {
                    result = context.lambdaExpressionParameters[text]
                    startFrom++
                }
            }
            (startFrom until callChain.size step 1)
                .forEach {
                    context.value = result
                    result = callChain[it]?.evaluate(context)
                }
        }
        return result
    }

    override fun toString(): String {
        return callChain.toString()
    }
}