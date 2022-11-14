package org.skellig.teststep.processing.validation.comparator

import org.skellig.teststep.processing.exception.ValidationException
import java.math.BigDecimal
import java.util.regex.Pattern

abstract class NumericValueComparator : ValueComparator {

    override fun compare(comparator: String, args: Array<Any?>, actualValue: Any?): Boolean {
        return if (actualValue is Number && actualValue is Comparable<*>) {
            val expectedExtracted = args[0].toString()
            try {
                when (actualValue) {
                    is BigDecimal -> compare(BigDecimal(actualValue.toBigInteger()), BigDecimal(expectedExtracted))
                    else -> compare(actualValue.toDouble(), expectedExtracted.toDouble())
                }
            } catch (ex: NumberFormatException) {
                throw ValidationException("Invalid number format in function '$comparator': '$expectedExtracted'")
            }
        } else false
    }

    private fun <T> compare(actualValue: Comparable<T>, valueToCompare: T): Boolean where T : Number {
        return compare(actualValue.compareTo(valueToCompare))
    }

    protected abstract fun compare(result: Int): Boolean
}

class LessThanValueComparator : NumericValueComparator() {

    override fun compare(result: Int): Boolean = result < 0

    override fun getName(): String = "less"
}

class LessOrEqualsValueComparator : NumericValueComparator() {

    override fun compare(result: Int): Boolean = result <= 0

    override fun getName(): String = "lessOrEqual"
}

class MoreThanValueComparator : NumericValueComparator() {

    override fun compare(result: Int): Boolean = result > 0

    override fun getName(): String = "more"
}

class MoreOrEqualsThanComparator : NumericValueComparator() {

    override fun compare(result: Int): Boolean = result >= 0

    override fun getName(): String = "moreOrEqual"
}