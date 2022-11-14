package org.skellig.teststep.processing.validation.comparator

import java.util.regex.Pattern

class NotValueComparator(private val valueComparator: ValueComparator) : ValueComparator {

    companion object {
        private val NOT_PATTERN = Pattern.compile("not\\((.+)\\)")
    }

    override fun compare(comparator: String, args: Array<Any?>, actualValue: Any?): Boolean {
        if (actualValue != null && args.size == 1) {
           return !valueComparator.compare(args[0]?.toString() ?: "", actualValue)
        }
        return false
    }

    override fun compare(expectedValue: Any?, actualValue: Any?): Boolean {
        if (actualValue != null) {
            val matcher = NOT_PATTERN.matcher(expectedValue?.toString() ?: "")
            if (matcher.find()) {
                val expectedValueAsString = matcher.group(1)
                return !valueComparator.compare(expectedValueAsString, actualValue)
            }
        }
        return false
    }

    override fun isApplicable(expectedValue: Any?): Boolean {
        return NOT_PATTERN.matcher(expectedValue?.toString() ?: "").matches()
    }

    override fun getName(): String = "not"
}