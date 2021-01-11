package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.converter.TestStepValueConverter
import java.util.regex.Pattern

class TestStepFactoryValueConverter(val testStepValueConverter: TestStepValueConverter?) {

    companion object {
        private val PARAMETER_REGEX = Pattern.compile("\\$\\{([\\w-_]+)(\\s*:\\s*(.+))?\\}")
    }

    fun <T> convertValue(value: Any?, parameters: Map<String, Any?>): T? {
        var result: Any? = value
        if (isString(value)) {
            result = applyParameters(value.toString(), parameters)
            if (isString(result)) {
                result = testStepValueConverter!!.convert(result.toString())
            }
        }
        return result as T?
    }

    private fun applyParameters(valueAsString: String, parameters: Map<String, Any?>): Any? {
        val matcher = PARAMETER_REGEX.matcher(valueAsString)
        var result: Any? = valueAsString
        if (matcher.find()) {
            val parameterName = matcher.group(1)
            val parameterValue = parameters.getOrDefault(parameterName, null)
            val hasDefaultValue = matcher.group(3) != null
            if (matcher.group(0).length != valueAsString.length) {
                if (parameterValue != null || !hasDefaultValue) {
                    result = valueAsString.replace(matcher.group(0), parameterValue.toString())
                } else {
                    var defaultValue = matcher.group(3)
                    defaultValue = convertValue<CharArray>(defaultValue, parameters).toString()
                    result = valueAsString.replace(matcher.group(0), defaultValue)
                }
            } else {
                result = if (parameterValue != null || !hasDefaultValue) {
                    parameterValue
                } else {
                    convertValue<Any>(matcher.group(3), parameters)
                }
            }
        }
        return result
    }

    private fun isString(value: Any?): Boolean {
        return value is String
    }
}