package org.skellig.teststep.reader.value.expression


/**
 * Represents a value expression that evaluates alphanumeric values.
 * There are 2 special values for AlphanumericValueExpression:
 * 1) $ - indicates a reference to the current value, usually used in validation of a Test Step.
 * 2) $result - indicating a reference to the result of a Test Step execution,
 *    usually used in direct data extraction from the result to the scenario state.
 *
 * @property value The alphanumeric value.
 */
class AlphanumericValueExpression(private val value: String) : ValueExpression {

    companion object {
        /**
         * Special character for the alphanum expression indicating a reference to the current value,
         * usually used in validation
         */
        const val THIS = "$"
        /**
         * Special character for the alphanum expression indicating a reference to the result of a test step execution,
         * usually used in direct data extraction from the result to the scenario state
         */
        const val RESULT = "\$result"
        private val setOfSpecialNames = setOf(THIS, RESULT)
    }

    override fun evaluate(context: ValueExpressionContext): Any? {
        return if (context.hasLambdaParameterWithName(value)) context.getLambdaExpressionParameter(value)
        else if (context.evaluationType == EvaluationType.CALL_CHAIN || setOfSpecialNames.contains(value))
            context.onFunctionCall(value, context.value, emptyArray())
        else value
    }

    override fun toString(): String {
        return value
    }

    override fun equals(other: Any?): Boolean {
        return if (other is AlphanumericValueExpression) value == other.value
        else false
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}