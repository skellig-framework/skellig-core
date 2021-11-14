package org.skellig.teststep.processing.converter

import org.apache.commons.lang3.StringUtils
import org.skellig.teststep.processing.exception.TestValueConversionException
import java.util.regex.Matcher
import java.util.regex.Pattern

class PropertyValueConverter(
    var valueConverter: TestStepValueConverter,
    private val propertyExtractorFunction: ((String) -> String?)?
) : TestStepValueConverter {

    companion object {
        private val PARAMETER_REGEX2 = Pattern.compile("\\$\\{([\\w-_.]+)(\\s*:\\s*(.*))?}|\\\$\\{(.+)}")
        private const val NULL = "null"
    }

    override fun convert(value: Any?): Any? =
        when (value) {
            is String -> {
                var group = ""
                var result = ""
                var isPropertyGroupActive = false
                var isPropertyFound = false
                var isDefaultValueActive = false
                var isPropertyKeyActive = false
                for (i in 0 until value.length) {
                    when (value[i]) {
                        ' ' -> {
                            if (value[i + 1] != ':' && value[i - 1] != ':' &&
                                value[i + 1] != '}' && value[i - 1] != '{'
                            ) {
                                group += value[i]
                            }
                        }
                        '$' -> {
                            if (value[i + 1] != '{') {
                                group += value[i]
                            }
                        }
                        '{' -> {
                            if (value[i - 1] != '\\') {
                                if (!isPropertyGroupActive) {
                                    isPropertyFound = false
                                }

                                if (!isPropertyFound) {
                                    result += group
                                }
                                group = ""
                                isPropertyGroupActive = true
                                isPropertyKeyActive = true
                            } else {
                                group += value[i]
                            }
                        }
                        '}' -> {
                            if (value[i - 1] != '\\') {
                                if (!isPropertyGroupActive && !isDefaultValueActive) {
                                    result += group
                                }
                                isPropertyGroupActive = false

                                if (!isPropertyFound) {
                                    if (isDefaultValueActive) {
                                        result += group
                                        isPropertyFound = true
                                    } else {
                                        val propertyValue = getPropertyValue(group)
                                        if (StringUtils.isNotEmpty(propertyValue)) {
                                            result += propertyValue
                                            isPropertyFound = true
                                        } else {
                                            throw TestValueConversionException("No value found for the property '$group'")
                                        }
                                    }
                                    isDefaultValueActive = false
                                }
                                group = ""
                            } else {
                                group += value[i]
                            }
                        }
                        ':' -> {
                            if (value[i - 1] != '\\') {
                                if (group.isNotEmpty() && !isPropertyKeyActive) {
                                    group += value[i]
                                } else if (!isPropertyFound) {
                                    if (isPropertyGroupActive) {
                                        val propertyValue = getPropertyValue(group)
                                        if (StringUtils.isNotEmpty(propertyValue)) {
                                            result += propertyValue
                                            isPropertyFound = true
                                            isDefaultValueActive = false
                                        } else {
                                            isDefaultValueActive = true
                                        }
                                        group = ""
                                    } else {
                                        group += value[i]
                                    }
                                    isPropertyKeyActive = false
                                } else {
                                    isDefaultValueActive = false
                                    isPropertyKeyActive = false
                                }
                            } else {
                                group += value[i]
                            }
                        }
                        '\\' -> {
                            if(i + 1 >= value.length ||
                                value[i + 1] != ':' && value[i + 1] != '}' && value[i + 1] != '{') {
                                group += value[i]
                            }
                        }
                        else -> {
                            group += value[i]
                        }
                    }
                }

                if (result == NULL) null else result + group
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