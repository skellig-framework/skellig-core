package org.skellig.teststep.processing.value.function

import java.util.regex.Pattern

class MatchValueComparator : FunctionValueExecutor {

    override fun execute(name: String, value: Any?, args: Array<Any?>): Boolean {
       return if (args.size == 1 && args[0] != null && value != null) {
            val regex = args[0].toString()
           isMatchRegex(regex, value.toString())
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

    override fun getFunctionName(): String = "match"
}