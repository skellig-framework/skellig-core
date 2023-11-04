package org.skellig.teststep.reader.sts.value.expression

class BooleanValueExpression(value: String) : ValueExpression {
    private val value: Boolean

    init {
        this.value = value.toBoolean()
    }

    override fun evaluate(): Any = value

    override fun toString(): String {
        return value.toString()
    }
}