package org.skellig.teststep.processing.value

import org.skellig.teststep.processing.validation.comparator.DefaultValueComparator
import org.skellig.teststep.processing.value.extractor.ValueExtractor
import org.skellig.teststep.processing.value.function.FunctionValueExecutor
import org.skellig.teststep.processing.value.property.PropertyExtractor
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression.Companion.THIS
import org.skellig.teststep.reader.value.expression.EvaluationType
import org.skellig.teststep.reader.value.expression.ValueExpressionContext

class ValueExpressionContextFactory(
    private val functionExecutor: FunctionValueExecutor,
    private val valueExtractor: ValueExtractor,
    private val referenceExtractor: PropertyExtractor
) {

    private val comparator = DefaultValueComparator.Builder().build()

    private val onFunctionCall = { name: String, ownerValue: Any?, args: Array<Any?> ->
        ownerValue?.let { valueExtractor.extractFrom(name, it, args) } ?: functionExecutor.execute(name, args)
    }

    fun create(parameters: Map<String, Any?>): ValueExpressionContext = ValueExpressionContext(EvaluationType.DEFAULT, onFunctionCall, createOnGetReferenceValue(parameters))

    fun createEmpty() = ValueExpressionContext()

    fun createForValidationAsCallChain(value: Any?, parameters: Map<String, Any?>): ValueExpressionContext {
        val context = ValueExpressionContext(
            EvaluationType.CALL_CHAIN,
            createOnFunctionCallForValidation(value),
            createOnGetReferenceValue(parameters)
        )
        context.value = value
        return context
    }

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
            else ownerValue?.let {
                if (comparator.isApplicable(name)) comparator.compare(name, args, it)
                else valueExtractor.extractFrom(name, it, args)
            } ?: functionExecutor.execute(name, args)
        }
}