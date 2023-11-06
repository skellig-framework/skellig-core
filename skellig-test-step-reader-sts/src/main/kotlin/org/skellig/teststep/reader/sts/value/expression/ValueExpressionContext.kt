package org.skellig.teststep.reader.sts.value.expression

class ValueExpressionContext(
    val functionCallDelegate: (name: String, args: Array<Any?>) -> Any?,
    val propertyCallDelegate: (key: String, default: () -> Any?) -> Any?,
) {
    var value: Any? = null
    private var lambdaExpressionParameters: MutableMap<String, Any?>? = null

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