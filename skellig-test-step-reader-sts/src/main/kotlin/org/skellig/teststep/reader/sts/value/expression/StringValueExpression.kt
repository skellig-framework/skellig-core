package org.skellig.teststep.reader.sts.value.expression

class StringValueExpression(private val value: String) : ValueExpression {
    override fun evaluate(): Any= value

    override fun toString(): String {
        return value
    }
}