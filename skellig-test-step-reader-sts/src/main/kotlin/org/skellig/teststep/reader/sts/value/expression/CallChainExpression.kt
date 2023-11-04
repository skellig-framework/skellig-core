package org.skellig.teststep.reader.sts.value.expression

class CallChainExpression(private val callChain: List<ValueExpression?>) : ValueExpression {

    override fun evaluate(): Any? {
        return null
    }

    override fun toString(): String {
        return callChain.toString()
    }
}