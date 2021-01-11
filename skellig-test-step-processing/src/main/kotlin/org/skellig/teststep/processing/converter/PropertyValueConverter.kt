package org.skellig.teststep.processing.converter

import org.apache.commons.lang3.StringUtils
import java.util.function.Function
import java.util.regex.Matcher
import java.util.regex.Pattern

class PropertyValueConverter(var valueConverters: List<TestStepValueConverter>,
                             val propertyExtractorFunction: Function<String, String?>?) : TestStepValueConverter {

    companion object {
        private val PARAMETER_REGEX = Pattern.compile("\\$\\{([\\w-_]+)(\\s*:\\s*(.+))?\\}")
    }

    init {
        if (valueConverters.none { javaClass == it.javaClass }) {
            valueConverters = mutableListOf(listOf(this), valueConverters).flatten()
        }
    }

    override fun convert(value: String?): Any? {
        return value?.let {
            var newValue = it
            val matcher = PARAMETER_REGEX.matcher(newValue)
            if (matcher.find()) {
                val propertyValue = getPropertyValue(matcher.group(1))
                if (StringUtils.isNotEmpty(propertyValue) || !hasDefaultValue(matcher)) {
                    newValue = newValue.replace(matcher.group(0), propertyValue)
                } else if (hasDefaultValue(matcher)) {
                    var defaultValue = matcher.group(3)
                    for (valueConverter in valueConverters) {
                        defaultValue = valueConverter.convert(defaultValue).toString()
                    }
                    newValue = newValue.replace(matcher.group(0), defaultValue!!)
                }
            }
            return newValue
        }
    }

    private fun getPropertyValue(propertyKey: String): String {
        var propertyValue: String? = null
        if (propertyExtractorFunction != null) {
            propertyValue = propertyExtractorFunction.apply(propertyKey)
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