package org.skellig.teststep.processing.validation.comparator

import org.skellig.teststep.processing.exception.ValidationException
import java.math.BigDecimal
import java.util.regex.Pattern

class NumericValueComparator : ValueComparator {

    companion object {
        private const val LESS = "lessThan"
        private const val MORE = "moreThan"
        private const val LESS_OR_EQUAL = "lessOrEqual"
        private const val MORE_OR_EQUAL = "moreOrEqual"
        private val PATTERN = Pattern.compile("($LESS|$MORE|$LESS_OR_EQUAL|$MORE_OR_EQUAL)\\((.+)\\)")
    }

    override fun compare(comparator: String, args: Array<Any?>, actualValue: Any?): Boolean {
        return if (actualValue is Number && actualValue is Comparable<*>) {
            val expectedExtracted = args[0].toString()
            try {
                when (actualValue) {
                    is BigDecimal -> compare(comparator, BigDecimal(actualValue.toBigInteger()), BigDecimal(expectedExtracted))
                    else -> compare(comparator, actualValue.toDouble(), expectedExtracted.toDouble())
                }
            } catch (ex: NumberFormatException) {
                throw ValidationException("Invalid number format in function '$comparator': '$expectedExtracted'")
            }
        } else false
    }

    override fun compare(expectedValue: Any?, actualValue: Any?): Boolean =
        if (actualValue is Number && actualValue is Comparable<*>) {
            val matcher = PATTERN.matcher(expectedValue?.toString() ?: "")
            if (matcher.find()) {
                val comparator = matcher.group(1)
                val expectedExtracted = matcher.group(2)
                try {
                    when (actualValue) {
                        is BigDecimal -> compare(comparator, BigDecimal(actualValue.toBigInteger()), BigDecimal(expectedExtracted))
                        else -> compare(comparator, actualValue.toDouble(), expectedExtracted.toDouble())
                    }
                } catch (ex: NumberFormatException) {
                    throw ValidationException("Invalid number format in function '$comparator': '$expectedExtracted'")
                }
            } else false
        } else false

    private fun <T> compare(comparator: String?, actualValue: Comparable<T>, valueToCompare: T): Boolean where T : Number {
        val result = actualValue.compareTo(valueToCompare)
        return when (comparator) {
            LESS -> result < 0
            MORE -> result > 0
            LESS_OR_EQUAL -> result <= 0
            MORE_OR_EQUAL -> result >= 0
            else -> false
        }
    }

    override fun isApplicable(expectedValue: Any?): Boolean =
        if (expectedValue is String) {
            expectedValue.startsWith(LESS) || expectedValue.startsWith(MORE) ||
                    expectedValue.startsWith(LESS_OR_EQUAL) || expectedValue.startsWith(MORE_OR_EQUAL)
        } else false

    override fun getName(): String = "less"
}