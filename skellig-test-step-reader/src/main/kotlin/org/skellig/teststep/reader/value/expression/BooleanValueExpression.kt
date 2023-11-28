package org.skellig.teststep.reader.value.expression

class BooleanValueExpression(value: String) : ValueExpression {
    private val value: Boolean

    init {
        this.value = value.toBoolean()
    }

    override fun evaluate(context: ValueExpressionContext): Any = value

    override fun toString(): String {
        return value.toString()
    }

    override fun equals(other: Any?): Boolean {
        return if (other is BooleanValueExpression) value == other.value
        else false
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}