package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.converter.TestStepValueConverter
import org.skellig.teststep.processing.valueextractor.TestStepValueExtractor

open class TestStepFactoryValueConverter(
    private val testStepValueConverter: TestStepValueConverter,
    valueExtractor : TestStepValueExtractor,
    getPropertyFunction: ((String) -> Any?)?
) {

    private val propertyParser = PropertyParser(getPropertyFunction, valueExtractor);

    open fun <T> convertValue(value: Any?, parameters: Map<String, Any?>): T? {
        var result: Any? = value
        result = processParametersAndProperties<T>(result, parameters)
        return testStepValueConverter.convert(result) as T?
    }

    private fun <T> processParametersAndProperties(result: Any?, parameters: Map<String, Any?>): Any? =
        when (result) {
            is Map<*, *> -> result.entries.map {
                propertyParser.parse(
                    it.key.toString(),
                    parameters
                ) to processParametersAndProperties<T>(it.value, parameters)
            }.toMap()
            is Collection<*> -> result.map { processParametersAndProperties<T>(it, parameters) }.toList()
            is String -> propertyParser.parse(result, parameters)
            else -> result
        }
}