package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.exception.TestValueConversionException

internal class PropertyParser(
    private val propertyExtractorFunction: ((String) -> String?)?
) {

    companion object {
        private const val NULL = "null"
    }

    // identify all parameters or properties and process them
    fun parse(value: Any?, parameters: Map<String, Any?>): Any? =
        when (value) {
            is String -> {
                var group = ""
                var result: Any? = null
                var isPropertyGroupActive = false
                var isPropertyFound = false
                var isDefaultValueActive = false
                var isPropertyKeyActive = false
                var isInsideQuotes = false
                for (i in 0 until value.length) {
                    when (value[i]) {
                        '"', '\'' -> {
                            if (i == 0 || value[i - 1] != '\\') {
                                isInsideQuotes = !isInsideQuotes
                            }
                            // we still need to keep ' or " because they will be passed to converter
                            group += value[i]
                        }
                        ' ' -> {
                            if (value[i + 1] != ':' && value[i - 1] != ':' &&
                                value[i + 1] != '}' && value[i - 1] != '{'
                            ) {
                                group += value[i]
                            }
                        }
                        '$' -> {
                            if (isInsideQuotes || value[i + 1] != '{') {
                                group += value[i]
                            }
                        }
                        '{' -> {
//                            if (!isInsideQuotes && value[i - 1] != '\\') {
                            if (!isInsideQuotes && i > 0 && value[i - 1] == '$') {
                                if (!isPropertyGroupActive) {
                                    isPropertyFound = false
                                }

                                if (!isPropertyFound) {
                                    result = if (group.isNotEmpty()) (result?.toString() ?: "") + group else result
                                }
                                group = ""
                                isPropertyGroupActive = true
                                isPropertyKeyActive = true
                            } else {
                                group += value[i]
                            }
                        }
                        '}' -> {
                            if (!isInsideQuotes && value[i - 1] != '\\') {
                                if (!isPropertyGroupActive && !isDefaultValueActive) {
                                    result = if (group.isNotEmpty()) (result?.toString() ?: "") + group else result
                                }
                                isPropertyGroupActive = false

                                if (!isPropertyFound) {
                                    if (isDefaultValueActive && !isPropertyKeyActive) {
                                        result = (result?.toString() ?: "") + group
                                        isPropertyFound = true
                                    } else {
                                        val propertyValue = getPropertyValue(group, parameters)
                                        getPropertyValue(group, parameters)?.let {
                                            result =
                                                if (result == null) propertyValue else result.toString() + propertyValue
                                            isPropertyFound = true
                                        }
                                            ?: throw TestValueConversionException("No value found for the property '$group'")
                                    }
                                    isDefaultValueActive = false
                                }
                                group = ""
                            } else {
                                group += value[i]
                            }
                        }
                        ':' -> {
                            if (!isInsideQuotes && value[i - 1] != '\\') {
                                if (group.isNotEmpty() && !isPropertyKeyActive) {
                                    group += value[i]
                                } else if (!isPropertyFound) {
                                    if (isPropertyGroupActive) {
                                        val propertyValue = getPropertyValue(group, parameters)
                                        getPropertyValue(group, parameters)?.let {
                                            result =
                                                if (result == null) propertyValue else result.toString() + propertyValue
                                            isPropertyFound = true
                                            isDefaultValueActive = false
                                        }.run { isDefaultValueActive = true }
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
                            if (i + 1 >= value.length ||
                                value[i + 1] != ':' && value[i + 1] != '}' && value[i + 1] != '{'
                            ) {
                                group += value[i]
                            }
                        }
                        else -> {
                            group += value[i]
                        }
                    }
                }

                if (result == NULL) null
                else if (result == null && group.isEmpty()) {
                    value
                } else if (group.isNotEmpty()) {
                    (result?.toString() ?: "") + group
                } else {
                    result
                }
            }
            else -> value
        }

    private fun getPropertyValue(propertyKey: String, parameters: Map<String, Any?>): Any? {
        var propertyValue: Any? = null
        if (propertyExtractorFunction != null) {
            propertyValue = propertyExtractorFunction.invoke(propertyKey)
        }
        if (propertyValue == null && parameters.containsKey(propertyKey)) {
            val value = parameters[propertyKey]
            if (!(value is String && value.isEmpty())) {
                propertyValue = parse(value, parameters)
            }
        }
        if (propertyValue == null) {
            propertyValue = System.getProperty(propertyKey)
        }
        if (propertyValue == null) {
            propertyValue = System.getenv(propertyKey)
        }
        return propertyValue
    }
}