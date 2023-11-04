package org.skellig.teststep.reader.sts.value.expression

class BooleanNotOperationExpression(private val valueExpression: ValueExpression) : ValueExpression {

    override fun evaluate(): Any {
        return !(valueExpression.evaluate() as Boolean?)!!
    }
}