package org.skellig.teststep.processing.value.function

import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import java.lang.String.format
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Class that executes the "fromRegex" function which extracts groups from a string using a regular expression and
 * returns a list of the extracted groups or a [String] if only 1 group is found.
 *
 * Supported args:
 * - fromRegex(`<regex>`) - for example: fromRegex("key = (\w+)") extracts a [String] value captured by (\w+)
 */
class FromRegexFunctionExecutor : FunctionValueExecutor {

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any {
        if (args.size == 1) {
            val extractionParameter = args[0]?.toString() ?: ""
            return value?.let {
                val matcher = Pattern.compile(extractionParameter).matcher(it as String)
                val result = extractGroups(matcher)
                return when {
                    result.size == 1 -> result.first()
                    result.size > 1 -> result
                    else -> value
                }
            } ?: throw FunctionExecutionException(format("Cannot extract '%s' from null value", extractionParameter))
        } else {
            throw FunctionExecutionException("Function `regex` can only accept 1 argument. Found ${args.size}")
        }
    }

    private fun extractGroups(matcher: Matcher): MutableList<String> {
        val result = mutableListOf<String>()
        while (matcher.find()) {
            for (i in (if (matcher.groupCount() == 0) 0 else 1)..matcher.groupCount()) {
                result.add(matcher.group(i))
            }
        }
        return result
    }

    override fun getFunctionName(): String {
        return "fromRegex"
    }
}