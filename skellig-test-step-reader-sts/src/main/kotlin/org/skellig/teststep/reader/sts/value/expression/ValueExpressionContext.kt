package org.skellig.teststep.reader.sts.value.expression

class ValueExpressionContext(
    var evaluationType: EvaluationType = EvaluationType.DEFAULT,
    val functionCallDelegate: (name: String, currentValue: Any?, args: Array<Any?>) -> Any?,
    val propertyCallDelegate: (key: String, default: () -> Any?) -> Any?,
) {
    var value: Any? = null
    private var lambdaExpressionParameters: MutableMap<String, Any?>? = null

    constructor(copy: ValueExpressionContext)
            :this(EvaluationType.DEFAULT, copy.functionCallDelegate, copy.propertyCallDelegate){
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