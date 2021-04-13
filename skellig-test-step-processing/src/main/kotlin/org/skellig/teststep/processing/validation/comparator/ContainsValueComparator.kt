package org.skellig.teststep.processing.validation.comparator

import java.util.regex.Pattern

class ContainsValueComparator : ValueComparator {

    companion object {
        private val CONTAINS_PATTERN = Pattern.compile("contains\\((.+)\\)")
    }

    override fun compare(expectedValue: Any?, actualValue: Any?): Boolean {
        if (actualValue != null) {
            val matcher = CONTAINS_PATTERN.matcher(expectedValue?.toString() ?: "")
            if (matcher.find()) {
                val expectedValueAsString = matcher.group(1)
                // usually actual is String so to speed up comparison it checks if it is String first
                return when {
                    actualValue::class == String::class -> {
                        (actualValue as String).contains(expectedValueAsString)
                    }
                    actualValue.javaClass.isArray -> {
                        compareArray(actualValue, expectedValueAsString)
                    }
                    actualValue is Collection<*> -> {
                        compareCollection(actualValue, expectedValueAsString)
                    }
                    else -> {
                        actualValue.toString().contains(expectedValueAsString)
                    }
                }
            }
        }
        return false
    }

    private fun compareCollection(actualValue: Collection<*>, expectedValueAsString: String?) = actualValue
            .map { it.toString() }
            .any { it == expectedValueAsString }

    private fun compareArray(actualValue: Any?, expectedValueAsString: String?) =
            if (actualValue is ByteArray) {
                actualValue
                        .map { it.toString() }
                        .any { it == expectedValueAsString }
            } else {
                (actualValue as Array<*>)
                        .map { it.toString() }
                        .any { it == expectedValueAsString }
            }

    override fun isApplicable(expectedValue: Any?): Boolean {
        return CONTAINS_PATTERN.matcher(expectedValue?.toString() ?: "").matches()
    }
}