package org.skellig.teststep.processing.validation.comparator

import java.util.regex.Pattern

class RegexValueComparator : ValueComparator {

    companion object {
        private val PATTERN = Pattern.compile("regex\\((.+)\\)")
        private const val REGEX_PREFIX = "regex("
    }

    override fun compare(expectedValue: Any?, actualValue: Any?): Boolean {
        return actualValue?.let {
            val matcher = PATTERN.matcher(expectedValue.toString())
            if (matcher.find()) {
                val regex = matcher.group(1)
                val actualValueAsString = actualValue.toString()
                isMatchRegex(regex, actualValueAsString)
            } else {
                false
            }
        } ?: false
    }

    private fun isMatchRegex(regex: String, actualValueAsString: String): Boolean {
        return try {
            val expectedPattern = Pattern.compile(regex)
            expectedPattern.matcher(actualValueAsString).matches()
        } catch (ex: Exception) {
            false
        }
    }

    override fun isApplicable(expectedValue: Any?): Boolean {
        return (expectedValue?.toString() ?: "").contains(REGEX_PREFIX)
    }
}