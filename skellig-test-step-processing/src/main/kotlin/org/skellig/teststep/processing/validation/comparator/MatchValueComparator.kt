package org.skellig.teststep.processing.validation.comparator

import java.util.regex.Pattern

class MatchValueComparator : ValueComparator {

    override fun compare(comparator: String, args: Array<Any?>, actualValue: Any?): Boolean {
       return if (args.size == 1 && args[0] != null && actualValue != null) {
            val regex = args[0].toString()
           isMatchRegex(regex, actualValue.toString())
        } else {
            false
        }
    }

    private fun isMatchRegex(regex: String, actualValueAsString: String): Boolean {
        return try {
            val expectedPattern = Pattern.compile(regex)
            expectedPattern.matcher(actualValueAsString).matches()
        } catch (ex: Exception) {
            false
        }
    }

    override fun getName(): String = "match"
}