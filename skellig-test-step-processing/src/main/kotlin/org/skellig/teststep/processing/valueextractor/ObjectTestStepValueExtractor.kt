package org.skellig.teststep.processing.valueextractor

import org.skellig.teststep.processing.exception.ValueExtractionException
import java.beans.IntrospectionException
import java.beans.Introspector
import java.lang.String.format
import java.lang.reflect.Array
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.regex.Pattern

class ObjectTestStepValueExtractor : TestStepValueExtractor {

    companion object {
        private val PATH_SEPARATOR = Pattern.compile("\\.")
        private val INDEX_PATTERN = Pattern.compile("\\[(\\d+)\\]")
    }

    override fun extract(value: Any?, extractionParameter: String?): Any? {
        value?.let {
            var newValue = value
            for (key in PATH_SEPARATOR.split(extractionParameter)) {
                if (newValue is Map<*, *>) {
                    newValue = extractValueFromMap(newValue, key)
                } else if (newValue is List<*> || newValue != null && newValue.javaClass.isArray) {
                    newValue = extractValueFromListOrArray(newValue, key)
                } else if (newValue != null) {
                    newValue = extractValueFromObject(key, newValue)
                }
            }
            return newValue
        } ?: throw ValueExtractionException(format("Cannot extract '%s' from null value", extractionParameter))
    }

    private fun extractValueFromListOrArray(value: Any, key: String): Any {
        val index = getIndex(key)
        return if (index >= 0) {
            if (value.javaClass.isArray) {
                Array.get(value, index)
            } else {
                (value as List<*>)[index]!!
            }
        } else {
            extractValueFromObject(key, value)
        }
    }

    private fun extractValueFromMap(value: Any, key: String): Any? {
        val valueAsMap = value as Map<*, *>
        return if (valueAsMap.containsKey(key)) {
            valueAsMap[key]
        } else {
            extractValueFromObject(key, value)
        }
    }

    private fun getIndex(value: String): Int {
        val matcher = INDEX_PATTERN.matcher(value)
        return if (matcher.find()) {
            matcher.group(1).toInt()
        } else {
            -1
        }
    }

    private fun extractValueFromObject(propertyName: String, actualResult: Any?): Any {
        var result: Any? = null
        val propertyGetter = getPropertyGetter(propertyName, actualResult!!.javaClass)
        if (propertyGetter != null) {
            result = try {
                propertyGetter.invoke(actualResult)
            } catch (e: IllegalAccessException) {
                throw ValueExtractionException(String.format("Failed to call property getter '%s' of %s",
                        propertyName, actualResult), e)
            } catch (e: InvocationTargetException) {
                throw ValueExtractionException(String.format("Failed to call property getter '%s' of %s",
                        propertyName, actualResult), e)
            }
        } else {
            val method = getMethod(propertyName, actualResult.javaClass)
            if (method != null) {
                result = try {
                    method.invoke(actualResult)
                } catch (e: IllegalAccessException) {
                     throw ValueExtractionException(String.format("Failed to call method '%s' of '%s'",
                            propertyName, actualResult), e)
                } catch (e: InvocationTargetException) {
                    throw ValueExtractionException(String.format("Failed to call method '%s' of '%s'",
                            propertyName, actualResult), e)
                }
            }
        }
        return result ?: throw ValueExtractionException(String.format("Failed to find property or method '%s' of '%s'",
                propertyName, actualResult.javaClass))
    }

    private fun getMethod(propertyName: String, resultClass: Class<*>): Method? {
        return try {
            resultClass.getMethod(propertyName)
        } catch (e: NoSuchMethodException) {
            null
        }
    }

    private fun getPropertyGetter(propertyName: String, beanClass: Class<*>): Method? {
        var method: Method? = null
        try {
            for (pd in Introspector.getBeanInfo(beanClass).propertyDescriptors) {
                if (pd.readMethod != null && propertyName == pd.name) {
                    method = pd.readMethod
                    break
                }
            }
        } catch (e: IntrospectionException) {
            throw ValueExtractionException(String.format("Failed to get property '%s' of '%s'", propertyName, beanClass), e)
        }
        return method
    }

    override fun getExtractFunctionName(): String? {
        return ""
    }
}