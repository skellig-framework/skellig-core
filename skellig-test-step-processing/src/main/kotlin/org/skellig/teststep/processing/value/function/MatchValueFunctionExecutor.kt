package org.skellig.teststep.processing.value.function

import java.util.regex.Pattern

/**
 * Executes 'match' function by comparing the 'value' with a regular expression pattern from arguments.
 * The 'value' will be converted to [String] even if it's not.
 *
 * Supported args:
 * - match(`<regex>`) - where `<regex>` is a regular expression to be used in matching with 'value'. Returns 'true' if
 * matches the regex or 'false' otherwise.
 */
class MatchValueFunctionExecutor : FunctionValueExecutor {

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