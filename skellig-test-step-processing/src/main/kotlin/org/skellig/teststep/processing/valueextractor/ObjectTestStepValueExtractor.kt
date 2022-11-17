package org.skellig.teststep.processing.valueextractor

import org.skellig.teststep.processing.exception.ValueExtractionException
import org.skellig.teststep.processing.experiment.ValueExtractor
import java.beans.IntrospectionException
import java.beans.Introspector
import java.lang.String.format
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.regex.Pattern


class ObjectTestStepValueExtractor : TestStepValueExtractor, ValueExtractor {

    companion object {
        private val PATH_SEPARATOR = Pattern.compile("\\.(?=(?:[^\"']*['\"][^'\"]*['\"])*[^'\"]*\$)")
    }

    override fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any? {
        return if (value != null) {
            if (name.isNotEmpty()) {
                executeMethod(name, value, args)
            } else if (args.isNotEmpty()) {
                var newValue: Any? = value
                args.mapNotNull { key -> processKey(key?.toString()) }
                    .forEach { key ->
                        if (newValue is Map<*, *>) {
                            newValue = extractValueFromMap(newValue as Map<*, *>, key)
                        } else if (newValue != null) {
                            newValue = extractValueFromObject(key, newValue)
                        }
                    }
                newValue
            } else throw ValueExtractionException("Invalid number of argument provided for extraction of value. Expected 1, found: ${args.size}")
        } else throw ValueExtractionException("Cannot extract '${args.firstOrNull() ?: ""}' from null value")
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
        var result: Any? = null
        val propertyGetter = getPropertyGetter(propertyName, actualResult!!.javaClass)
        if (propertyGetter != null) {
            result = try {
                propertyGetter.invoke(actualResult)
            } catch (e: IllegalAccessException) {
                throw ValueExtractionException("Failed to call property getter `$propertyName` of `$actualResult`", e)
            } catch (e: InvocationTargetException) {
                throw ValueExtractionException("Failed to call property getter `$propertyName` of `$actualResult`", e)
            }
        } /*else {
            result = executeMethod(propertyName, actualResult)
        }*/
        return result ?: throw ValueExtractionException("Failed to find property or method `$propertyName` of `${actualResult.javaClass}`")
    }

    private fun executeMethod(methodName: String, actualResult: Any, args: Array<Any?>): Any? {
        val method = findMethod(methodName, actualResult.javaClass, args.mapNotNull { it?.javaClass }.toTypedArray())
        return if (method != null) {
            try {
                method.invoke(actualResult, *args)
            } catch (e: IllegalAccessException) {
                throw ValueExtractionException("Failed to call function `$methodName` of `$actualResult`", e)
            } catch (e: InvocationTargetException) {
                throw ValueExtractionException("Failed to call function `$methodName` of `$actualResult`", e)
            }
        } else throw ValueExtractionException("No function `$methodName` found in the result `$actualResult`")
    }

    private fun findMethod(name: String, resultClass: Class<*>, paramsTypes: Array<Class<*>>): Method? =
        resultClass.methods.firstOrNull {
            it.name == name && it.parameterTypes.size == paramsTypes.size &&
                    it.parameterTypes.filterIndexed { index, c -> isSameOrSubclass(paramsTypes[index], c) }.size == paramsTypes.size
        }

    private fun isSameOrSubclass(paramType: Class<*>, methodParam: Class<*>): Boolean {
        return paramType == methodParam ||
                ((paramType.isPrimitive || methodParam.isPrimitive) && paramType.simpleName.equals(methodParam.simpleName, true)) ||
                methodParam.isInterface && paramType.interfaces.toSet().contains(methodParam) ||
                (paramType.superclass != null && isSameOrSubclass(paramType.superclass, methodParam))
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