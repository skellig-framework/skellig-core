package org.skellig.teststep.reader.value.expression

/**
 * A ValueExpressionContext represents the context in which a value expression is evaluated.
 *
 * @property evaluationType The type of evaluation for the value expression context. It is [EvaluationType.DEFAULT] by default.
 * @property onFunctionCall A delegate that is called when a function is invoked within the value expression (ex. [FunctionCallExpression]).
 * @property onGetReferenceValue A delegate that is called to get the value of a reference within the value expression (ex. [PropertyValueExpression]).
 * @property value The current evaluated value within the context.
 * @property lambdaExpressionParameters A map of lambda expression parameters within the context.

 */
class ValueExpressionContext(
    var evaluationType: EvaluationType = EvaluationType.DEFAULT,
    val onFunctionCall: (name: String, currentValue: Any?, args: Array<Any?>) -> Any?,
    val onGetReferenceValue: (refName: String, default: () -> Any?) -> Any?,
) {
    companion object {
        val EMPTY = ValueExpressionContext()
    }

    var value: Any? = null
    private var lambdaExpressionParameters: MutableMap<String, Any?>? = null

    constructor()
            : this(EvaluationType.DEFAULT, { _, _, _ -> }, { _, _ -> })

    /**
     * Represents a constructor for the ValueExpressionContext class.
     * It creates a new instance of the class with the given value.
     *
     * @param value The value to assign to the ValueExpressionContext.
     */
    constructor(value: Any?)
            : this() {
        this.value = value
    }

    /**
     * Constructs a new instance of ValueExpressionContext by copying an existing instance.
     * It's not a deep copy and keeps the original references.*
     *
     * @param copy The ValueExpressionContext to be copied.
     */
    constructor(copy: ValueExpressionContext)
            : this(EvaluationType.DEFAULT, copy.onFunctionCall, copy.onGetReferenceValue) {
        this.value = copy.value
        this.lambdaExpressionParameters = copy.lambdaExpressionParameters
    }

    /**
     * Checks if the given lambda expression parameter name exists in the context.
     *
     * @param value The name of the lambda expression parameter to check.
     * @return `true` if the lambda expression parameter with the given name exists, `false` otherwise.
     */
    fun hasLambdaParameterWithName(value: String): Boolean =
        lambdaExpressionParameters != null && lambdaExpressionParameters!!.containsKey(value)

    /**
     * Sets the value of a lambda expression parameter in the context.
     *
     * @param name The name of the lambda expression parameter.
     * @param value The value to set for the lambda expression parameter.
     */
    fun setLambdaExpressionParameter(name: String, value: Any?) {
        if (lambdaExpressionParameters == null) {
            lambdaExpressionParameters = mutableMapOf()
        }
        lambdaExpressionParameters!![name] = value
    }

    /**
     * Retrieves the value of a lambda expression parameter from the context.
     *
     * @param name The name of the lambda expression parameter.
     * @return The value of the lambda expression parameter, or null if it doesn't exist.
     */
    fun getLambdaExpressionParameter(name: String): Any? =
        lambdaExpressionParameters?.let { it[name] }
}

/**
 * Represents the types of evaluation that can be performed on a value expression.
 */
enum class EvaluationType {
    /**
     * The DEFAULT class represents the types of evaluation that can be performed on a value expression.
     */
    DEFAULT,

    /**
     * Represents a type of evaluation for call chain expression as well as other expressions which can be part of it.
     * For example, [CallChainExpression] can include [AlphanumericValueExpression] and [StringValueExpression], for example:
     * func1().a."b.c".last(), where 'a' is [AlphanumericValueExpression] and "b.c" is [StringValueExpression].
     * Since [AlphanumericValueExpression] and [StringValueExpression] can be also individual expressions, not part of
     * a call chain, this execution type can indicate that at the time of evaluation of [AlphanumericValueExpression] or [StringValueExpression]
     * they belong to the previous result of the call chain, rather than as individual expressions.
     */
    CALL_CHAIN
}