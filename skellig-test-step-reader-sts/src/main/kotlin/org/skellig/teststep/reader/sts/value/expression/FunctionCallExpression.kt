package org.skellig.teststep.reader.sts.value.expression

class FunctionCallExpression(private val name: String, private val args: List<ValueExpression?>) : ValueExpression {

    override fun evaluate(): Any? {
        return null
    }

    override fun toString(): String {
        return "$name($args)"
    }
}