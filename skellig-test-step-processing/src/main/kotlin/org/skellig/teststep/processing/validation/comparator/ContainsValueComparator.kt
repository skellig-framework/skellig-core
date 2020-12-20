package org.skellig.teststep.processing.validation.comparator

import java.util.*
import java.util.regex.Pattern

class ContainsValueComparator : ValueComparator {

    companion object {
        private val CONTAINS_PATTERN = Pattern.compile("contains\\((.+)\\)")
    }

    override fun compare(expectedValue: Any, actualValue: Any?): Boolean {
        if (actualValue != null) {
            val matcher = CONTAINS_PATTERN.matcher(expectedValue.toString())
            if (matcher.find()) {
                val expectedValueAsString = matcher.group(1)
                // usually actual is String so to speed up comparison it checks if it is String first
                return when {
                    actualValue.javaClass == String::class.java -> {
                        (actualValue as String).contains(expectedValueAsString)
                    }
                    actualValue.javaClass.isArray -> {
                        Arrays.stream(actualValue as Array<*>)
                                .map { it.toString() }
                                .anyMatch { item: String -> item == expectedValueAsString }
                    }
                    actualValue is Collection<*> -> {
                        actualValue
                                .map { it.toString() }
                                .any { item: String -> item == expectedValueAsString }
                    }
                    else -> {
                        actualValue.toString().contains(expectedValueAsString)
                    }
                }
            }
        }
        return false
    }

    override fun isApplicable(expectedValue: Any): Boolean {
        return CONTAINS_PATTERN.matcher(expectedValue.toString()).matches()
    }
}