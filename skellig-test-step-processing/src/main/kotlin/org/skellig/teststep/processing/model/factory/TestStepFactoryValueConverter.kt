package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.converter.TestStepValueConverter
import java.util.regex.Pattern

class TestStepFactoryValueConverter(val testStepValueConverter: TestStepValueConverter) {

    companion object {
        private val PARAMETER_REGEX = Pattern.compile("\\$\\{([\\w-_]+)(\\s*:\\s*(.+))?}")
    }

    /**
     * Convert the value by applying parameters first and then use provided testStepValueConverter
     * to convert it further if applicable
     */
    fun <T> convertValue(value: Any?, parameters: Map<String, Any?>): T? {
        var result: Any? = value
        result = when (result) {
            is Map<*, *> -> result.entries.map { applyParameters(it.key.toString(), parameters) to convertValue<T>(it.value, parameters) }.toMap()
            is Collection<*> -> result.map { convertValue<T>(it, parameters) }.toList()
            is String -> applyParameters(result.toString(), parameters)
            else -> result
        }
        result = testStepValueConverter.convert(result)
        return result as T?
    }

    private fun applyParameters(valueAsString: String, parameters: Map<String, Any?>): Any? {
        val matcher = PARAMETER_REGEX.matcher(valueAsString)
        var result: Any? = valueAsString
        if (matcher.find()) {
            val parameterName = matcher.group(1)
            val parameterValue = parameters[parameterName]
            val hasDefaultValue = matcher.group(3) != null
            if (matcher.group(0).length != valueAsString.length) {
                if (hasValue(parameterValue) || !hasDefaultValue) {
                    result = valueAsString.replace(matcher.group(0), parameterValue.toString())
                } else {
                    var defaultValue = matcher.group(3)
                    defaultValue = convertValue<CharArray>(defaultValue, parameters).toString()
                    result = valueAsString.replace(matcher.group(0), defaultValue)
                }
            } else {
                result = if (hasValue(parameterValue) || !hasDefaultValue) {
                    parameterValue
                } else {
                    convertValue<Any>(matcher.group(3), parameters)
                }
            }

            if (result != valueAsString && result is String) {
                result = applyParameters(result, parameters)
            }
        }
        return result
    }

    private fun hasValue(parameterValue: Any?) =
            when (parameterValue) {
                is String -> parameterValue.isNotEmpty()
                else -> parameterValue != null
            }
}