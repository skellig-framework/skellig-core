package org.skellig.teststep.processing.converter

import org.apache.commons.lang3.StringUtils
import java.util.regex.Matcher
import java.util.regex.Pattern

class PropertyValueConverter(var valueConverters: List<TestStepValueConverter>,
                             val propertyExtractorFunction: ((String) -> String?)?) : TestStepValueConverter {

    companion object {
        private val PARAMETER_REGEX = Pattern.compile("\\$\\{([\\w-_.]+)(\\s*:\\s*(.+))?}")
        private const val NULL = "null"
    }

    init {
        if (valueConverters.none { javaClass == it.javaClass }) {
            valueConverters = mutableListOf(listOf(this), valueConverters).flatten()
        }
    }

    override fun convert(value: String?): Any? {
        return value?.let {
            val matcher = PARAMETER_REGEX.matcher(it)
            return if (matcher.find()) convert(it, matcher) else value
        }
    }

    private fun convert(value: String, matcher: Matcher): Any? {
        var result: Any? = null
        val propertyValue = getPropertyValue(matcher.group(1))
        if (StringUtils.isNotEmpty(propertyValue) || !hasDefaultValue(matcher)) {
            result = value.replace(matcher.group(0), propertyValue)
        } else if (hasDefaultValue(matcher)) {
            var defaultValue = matcher.group(3)
            if (NULL != defaultValue) {
                for (valueConverter in valueConverters) {
                    defaultValue = valueConverter.convert(defaultValue).toString()
                }
                result = value.replace(matcher.group(0), defaultValue!!)
            }
        }
        return result
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

    private fun hasDefaultValue(matcher: Matcher): Boolean {
        return matcher.group(3) != null
    }
}