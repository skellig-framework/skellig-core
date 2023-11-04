package org.skellig.teststep.reader.sts.value.expression

class PropertyValueExpression(private val name: String, private val defaultValue: ValueExpression) : ValueExpression {

    override fun evaluate(): Any? {
        return null
    }

    override fun toString(): String {
        return "\${$name: $defaultValue}"
    }
}