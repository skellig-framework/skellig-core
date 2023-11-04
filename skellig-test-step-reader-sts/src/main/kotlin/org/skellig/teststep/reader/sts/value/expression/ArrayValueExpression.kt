package org.skellig.teststep.reader.sts.value.expression

class ArrayValueExpression(private val name: String, private val index: Int) : ValueExpression {

    override fun evaluate(): Any? {
        return null
    }

    override fun toString(): String {
        return "$name[$index]"
    }
}