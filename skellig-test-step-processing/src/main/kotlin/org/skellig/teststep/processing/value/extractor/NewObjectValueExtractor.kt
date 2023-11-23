package org.skellig.teststep.processing.value.extractor

import org.skellig.teststep.processing.exception.ValueExtractionException
import java.beans.IntrospectionException
import java.beans.Introspector
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.regex.Pattern


class NewObjectValueExtractor : ValueExtractor {

    companion object {
        private val INDEX_PATTERN = Pattern.compile("(.+)\\[(\\d+)]")
    }

    override fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any? {
        if (value != null) {
            if (name.isNotEmpty()) {
                var updatedKey = name
                var index: Int? = null
                val matcher = INDEX_PATTERN.matcher(name)
                if (matcher.find()) {
                    updatedKey = matcher.group(1)
                    index = matcher.group(2).toInt()
                }

                val newValue = if (value is Map<*, *>) {
                    extractValueFromMap(value, updatedKey, args)
                } else {
                    extractValueFromObject(updatedKey, value, args)
                }

                index?.let {
                    return when (newValue) {
                        is List<*> -> newValue[index]
                        is Array<*> -> newValue[index]
                        else -> throw ValueExtractionException("Cannot get value by index '$index' from non array or list object: $newValue")
                    }
                }
                return newValue
            } else throw ValueExtractionException("Invalid number of argument provided for extraction of value. Expected 1, found: ${args.size}")
        } else throw ValueExtractionException("Cannot extract '${args.firstOrNull() ?: ""}' from null value")
    }

    private fun extractValueFromMap(value: Any, key: String, args: Array<Any?>): Any? {
        val valueAsMap = value as Map<*, *>
        return if (valueAsMap.containsKey(key)) {
            valueAsMap[key]
        } else {
            extractValueFromObject(key, value, args)
        }
    }

    private fun extractValueFromObject(propertyName: String, actualResult: Any?, args: Array<Any?>): Any {
        val propertyGetter = getPropertyGetter(propertyName, actualResult!!.javaClass)
        val result = if (propertyGetter != null) {
            try {
                propertyGetter.invoke(actualResult)
            } catch (e: IllegalAccessException) {
                throw ValueExtractionException("Failed to call property getter `$propertyName` of `$actualResult`", e)
            } catch (e: InvocationTargetException) {
                throw ValueExtractionException("Failed to call property getter `$propertyName` of `$actualResult`", e)
            }
        } else executeMethod(propertyName,actualResult, args)
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

    override fun getExtractFunctionName(): String {
        return ""
    }
}