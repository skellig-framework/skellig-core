package org.skellig.teststep.processing.valueextractor

import org.skellig.teststep.processing.exception.ValueExtractionException
import java.beans.IntrospectionException
import java.beans.Introspector
import java.lang.String.format
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.regex.Pattern

class ObjectTestStepValueExtractor : TestStepValueExtractor {

    companion object {
        private val PATH_SEPARATOR = Pattern.compile("\\.(?=(?:[^\"']*['\"][^'\"]*['\"])*[^'\"]*\$)")
    }

    override fun extract(value: Any?, extractionParameter: String?): Any? {
        value?.let {
            var newValue: Any? = it
            PATH_SEPARATOR.split(extractionParameter)
                    .mapNotNull { key -> processKey(key) }
                    .forEach { key ->
                        if (newValue is Map<*, *>) {
                            newValue = extractValueFromMap(newValue as Map<*, *>, key)
                        } else if (newValue != null) {
                            newValue = extractValueFromObject(key, newValue)
                        }
                    }
            return newValue
        } ?: throw ValueExtractionException(format("Cannot extract '%s' from null value", extractionParameter))
    }

    private fun extractValueFromMap(value: Any, key: String): Any? {
        val valueAsMap = value as Map<*, *>
        return if (valueAsMap.containsKey(key)) {
            valueAsMap[key]
        } else {
            extractValueFromObject(key, value)
        }
    }

    private fun extractValueFromObject(propertyName: String, actualResult: Any?): Any {
        val result: Any?
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
            result = if (method != null) {
                try {
                    method.invoke(actualResult)
                } catch (e: IllegalAccessException) {
                    throw ValueExtractionException(String.format("Failed to call method '%s' of '%s'",
                            propertyName, actualResult), e)
                } catch (e: InvocationTargetException) {
                    throw ValueExtractionException(String.format("Failed to call method '%s' of '%s'",
                            propertyName, actualResult), e)
                }
            } else throw ValueExtractionException(String.format("Failed to find property or method '%s' of '%s'",
                propertyName, actualResult.javaClass))
        }
        return result
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

    private fun processKey(key: String?): String? {
        return key?.let {
            return if (key.startsWith('\'') && key.endsWith('\'')) {
                key.substringAfter('\'').substringBeforeLast('\'')
            } else if (key.startsWith('\"') && key.endsWith('\"')) {
                key.substringAfter('\"').substringBeforeLast('\"')
            } else {
                key
            }
        }
    }

    override fun getExtractFunctionName(): String {
        return ""
    }
}