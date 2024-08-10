package org.skellig.teststep.reader.value.expression

object ValueExpressionObject {

    fun alphaNum(value: String) = AlphanumericValueExpression(value)

    fun string(value: String) = StringValueExpression(value)

    fun funcCall(name: String, args: Array<ValueExpression?> = emptyArray()) = FunctionCallExpression(name, args)

    fun callChain(vararg callChainExpressions: ValueExpression) = CallChainExpression(callChainExpressions.toList())

    fun num(number: String) = NumberValueExpression(number)

    fun bool(value: String) = BooleanValueExpression(value)

    fun map(vararg items: Pair<ValueExpression, ValueExpression?>) = MapValueExpression(items.toMap())

    fun list(vararg items: ValueExpression) = ListValueExpression(items.toList())

    fun list(items: List<ValueExpression?>) = ListValueExpression(items)

    fun ref(key: ValueExpression, defaultValue: ValueExpression? = null) = PropertyValueExpression(key, defaultValue)

    fun ref(key: String, defaultValue: ValueExpression? = null) = PropertyValueExpression(key, defaultValue)

    fun mathOp(operator: String, leftExpression: ValueExpression, rightExpression: ValueExpression) =
        MathOperationExpression(operator, leftExpression, rightExpression)

    fun compare(operator: String, leftExpression: ValueExpression, rightExpression: ValueExpression) =
        ValueComparisonExpression(operator, leftExpression, rightExpression)

    fun boolOp(operator: String, leftExpression: ValueExpression, rightExpression: ValueExpression) =
        BooleanOperationExpression(operator, leftExpression, rightExpression)
}