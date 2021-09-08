package org.skellig.teststep.processing.converter

import org.apache.commons.lang3.StringUtils
import java.util.regex.Matcher
import java.util.regex.Pattern

class PropertyValueConverter(var valueConverter: TestStepValueConverter,
                             private val propertyExtractorFunction: ((String) -> String?)?) : TestStepValueConverter {

    companion object {
        private val PARAMETER_REGEX = Pattern.compile("\\$\\{([\\w-_.]+)(\\s*:\\s*(.*))?}|\\\$\\{(.+)}")
        private const val NULL = "null"
    }

    override fun convert(value: Any?): Any? =
            when (value) {
                is String -> {
                    val matcher = PARAMETER_REGEX.matcher(value.toString())
                    if (matcher.find()) convert(value.toString(), matcher) else value
                }
                else -> value
            }

    private fun convert(value: String, matcher: Matcher): Any? {
        var result: Any? = null
        if (hasKeyOnly(matcher)) {
            result = replace(value, matcher.group(0), valueConverter.convert(matcher.group(4)));
        } else {
            val propertyValue = getPropertyValue(matcher.group(1))
            if (StringUtils.isNotEmpty(propertyValue) || !hasDefaultValue(matcher)) {
                result = value.replace(matcher.group(0), propertyValue)
            } else if (hasDefaultValue(matcher)) {
                val defaultValue = matcher.group(3)
                if (NULL != defaultValue) {
                    result = replace(value, matcher.group(0), valueConverter.convert(defaultValue));
                }
            }
        }
        return result
    }

    private fun replace(originalValue: String, capturedValue: String, newValue: Any?): Any? {
        return if (originalValue == capturedValue) {
            newValue
        } else {
            originalValue.replace(capturedValue, newValue.toString())
        }
    }

    private fun getPropertyValue(propertyKey: String): String {
        var propertyValue: String? = null
        if (propertyExtractorFunction != null) {
            propertyValue = propertyExtractorFunction.invoke(propertyKey)
        }
        if (propertyValue == null) {
            propertyValue = System.getProperty(propertyKey)
        }
        if (propertyValue == null) {
            propertyValue = System.getenv(propertyKey)
        }
        return propertyValue ?: ""
    }

    private fun hasDefaultValue(matcher: Matcher): Boolean = matcher.group(3) != null

    private fun hasKeyOnly(matcher: Matcher) = matcher.group(4) != null
}