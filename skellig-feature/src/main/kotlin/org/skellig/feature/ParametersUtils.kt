package org.skellig.feature

import java.util.regex.Pattern

internal class ParametersUtils {
    companion object {
        private val PARAM_REGEX = Pattern.compile("<([\\w_-]+)>")

        fun replaceParametersIfFound(value: String, data: Map<String, String?>): String {
            var newValue = value
            val matcher = PARAM_REGEX.matcher(newValue)
            while (matcher.find()) {
                val paramNameWithBrackets = matcher.group(0)
                val paramName = matcher.group(1)
                val paramValue = data.getOrDefault(paramName, "")
                newValue = newValue.replace(paramNameWithBrackets, paramValue!!)
            }
            return newValue
        }
    }
}