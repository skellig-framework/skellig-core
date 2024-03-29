package org.skellig.teststep.reader.value.expression

class ValueExpressionContext(
    var evaluationType: EvaluationType = EvaluationType.DEFAULT,
    val onFunctionCall: (name: String, currentValue: Any?, args: Array<Any?>) -> Any?,
    val onGetReferenceValue: (refName: String, default: () -> Any?) -> Any?,
) {
    companion object {
        val EMPTY = ValueExpressionContext()
    }

    var value: Any? = null
    var args: Array<Any?>? = null
    private var lambdaExpressionParameters: MutableMap<String, Any?>? = null

    constructor()
            : this(EvaluationType.DEFAULT, { _, _, _ -> }, { _, _ -> })

    constructor(value: Any?)
            : this() {
        this.value = value
    }

    constructor(copy: ValueExpressionContext)
            : this(EvaluationType.DEFAULT, copy.onFunctionCall, copy.onGetReferenceValue) {
        this.value = copy.value
        this.lambdaExpressionParameters = copy.lambdaExpressionParameters
    }

    fun hasLambdaParameterWithName(value: String): Boolean =
        lambdaExpressionParameters != null && lambdaExpressionParameters!!.containsKey(value)

    fun setLambdaExpressionParameter(name: String, value: Any?) {
        if (lambdaExpressionParameters == null) {
            lambdaExpressionParameters = mutableMapOf()
        }
        lambdaExpressionParameters!![name] = value
    }

    fun getLambdaExpressionParameter(name: String): Any? =
        lambdaExpressionParameters?.let { it[name] }
}

enum class EvaluationType {
    DEFAULT,
    CALL_CHAIN
}