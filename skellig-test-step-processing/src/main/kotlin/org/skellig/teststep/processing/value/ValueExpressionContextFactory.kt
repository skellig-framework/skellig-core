package org.skellig.teststep.processing.value

import org.skellig.teststep.processing.value.function.FunctionValueExecutor
import org.skellig.teststep.processing.value.property.PropertyExtractor
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression.Companion.RESULT
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression.Companion.THIS
import org.skellig.teststep.reader.value.expression.EvaluationType
import org.skellig.teststep.reader.value.expression.ValueExpressionContext

/**
 * Class representing a factory for creating instances of [ValueExpressionContext] which is used in evaluation of
 * [ValueExpression][org.skellig.teststep.reader.value.expression.ValueExpression] when calling method
 * [evaluate][org.skellig.teststep.reader.value.expression.ValueExpression.evaluate].
 *
 * @param functionExecutor The implementation of [FunctionValueExecutor] to be used for executing functions.
 * It's passed to a created [ValueExpressionContext] and particularly is used in evaluation of
 * [FunctionCallExpression][org.skellig.teststep.reader.value.expression.FunctionCallExpression]
 *
 * @param referenceExtractor The implementation of [PropertyExtractor] to be used for extracting property values.
 * It's passed to a created [ValueExpressionContext] and particularly is used in evaluation of
 * [PropertyValueExpression][org.skellig.teststep.reader.value.expression.PropertyValueExpression]
 */
class ValueExpressionContextFactory(
    private val functionExecutor: FunctionValueExecutor,
    private val referenceExtractor: PropertyExtractor
) {

    private val onFunctionCall = { name: String, ownerValue: Any?, args: Array<Any?> ->
        functionExecutor.execute(name, ownerValue, args)
    }

    /**
     * Creates a new instance of [ValueExpressionContext] using the provided parameters.
     *
     * The created [ValueExpressionContext] has [ValueExpressionContext.evaluationType] as [EvaluationType.DEFAULT].
     */
    fun create(parameters: Map<String, Any?>): ValueExpressionContext = ValueExpressionContext(EvaluationType.DEFAULT, onFunctionCall, createOnGetReferenceValue(parameters))

    /**
     * Creates a new [ValueExpressionContext] instance using the provided parameters and result to be used in
     * [FunctionValueExecutor] as 'value' if applicable, when evaluating [FunctionCallExpression][org.skellig.teststep.reader.value.expression.FunctionCallExpression].
     *
     * The created [ValueExpressionContext] has [ValueExpressionContext.evaluationType] as [EvaluationType.DEFAULT].
     *
     * @param result The result to be used when creating the context.
     * @param parameters The map of parameters to be used in the context.
     * @return The created [ValueExpressionContext] instance.
     */
    fun create(result: Any?, parameters: Map<String, Any?>): ValueExpressionContext =
        ValueExpressionContext(
            EvaluationType.DEFAULT,
            createOnFunctionCallForResult(result),
            createOnGetReferenceValue(parameters)
        )

    fun createEmpty() = ValueExpressionContext()

    /**
     * Creates a [ValueExpressionContext] for validation as part of a call chain. This is usually used to evaluate
     * [ValueExpression][org.skellig.teststep.reader.value.expression.ValueExpression] in validation details of a test step,
     * where actual value is [CallChainExpression][org.skellig.teststep.reader.value.expression.CallChainExpression].
     *
     * The created [ValueExpressionContext] has [ValueExpressionContext.evaluationType] as [EvaluationType.CALL_CHAIN].
     * @param value The current evaluated value within the context.
     * @param parameters The map of parameters to be used in the context.
     * @return The created [ValueExpressionContext] instance.
     */
    fun createForValidationAsCallChain(value: Any?, parameters: Map<String, Any?>): ValueExpressionContext {
        val context = ValueExpressionContext(
            EvaluationType.CALL_CHAIN,
            createOnFunctionCallForValidation(value),
            createOnGetReferenceValue(parameters)
        )
        context.value = value
        return context
    }

    /**
     * Creates a [ValueExpressionContext] for validation as part of a call chain. This is usually used to evaluate
     * [ValueExpression][org.skellig.teststep.reader.value.expression.ValueExpression] in validation details of a test step.
     *
     * The created [ValueExpressionContext] has [ValueExpressionContext.evaluationType] as [EvaluationType.DEFAULT].
     * @param value The current evaluated value within the context.
     * @param parameters The map of parameters to be used in the context.
     * @return The created [ValueExpressionContext] instance.
     */
    fun createForValidation(value: Any?, parameters: Map<String, Any?>, assignValueToContext: Boolean): ValueExpressionContext {
        val context = ValueExpressionContext(
            EvaluationType.DEFAULT,
            createOnFunctionCallForValidation(value),
            createOnGetReferenceValue(parameters)
        )
        if (assignValueToContext) context.value = value
        return context
    }

    private fun createOnGetReferenceValue(parameters: Map<String, Any?>) =
        { name: String, default: () -> Any? ->
            referenceExtractor.extractFrom(name, parameters) ?: default()
        }

    private fun createOnFunctionCallForValidation(parentValue: Any?) =
        { name: String, ownerValue: Any?, args: Array<Any?> ->
            if (name == THIS) parentValue
            else functionExecutor.execute(name, ownerValue, args)
        }

    private fun createOnFunctionCallForResult(parentValue: Any?) =
        { name: String, ownerValue: Any?, args: Array<Any?> ->
            if (name == RESULT) parentValue
            else functionExecutor.execute(name, ownerValue, args)
        }
}