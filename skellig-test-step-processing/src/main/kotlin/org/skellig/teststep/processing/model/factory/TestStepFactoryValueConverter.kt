package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.converter.TestStepValueConverter
import org.skellig.teststep.processing.valueextractor.TestStepValueExtractor

open class TestStepFactoryValueConverter private constructor(
    private val testStepValueConverter: TestStepValueConverter,
    valueExtractor: TestStepValueExtractor,
    getPropertyFunction: ((String) -> Any?)?
) {

    companion object {
        private val notToParseValues = mutableSetOf<String>()
    }

    private val propertyParser = PropertyParser(getPropertyFunction, valueExtractor);

    open fun <T> convertValue(value: Any?, parameters: Map<String, Any?>): T? {
        var result: Any? = value
        result = processParametersAndProperties<T>(result, parameters)
        return testStepValueConverter.convert(result) as T?
    }

    private fun <T> processParametersAndProperties(result: Any?, parameters: Map<String, Any?>): Any? =
        when (result) {
            is Map<*, *> -> result.entries.map {
                val newKey = propertyParser.parse(it.key.toString(), parameters)
                newKey to processParametersAndProperties<T>(it.value, parameters)
            }.toMap()
            is Collection<*> -> result.map { processParametersAndProperties<T>(it, parameters) }.toList()
            is String -> {
                if (!notToParseValues.contains(result)) {
                    val newResult = propertyParser.parse(result, parameters)
                    if (newResult == result) notToParseValues.add(result)
                    newResult
                } else result
            }
            else -> result
        }

    class Builder {
        private var testStepValueConverter: TestStepValueConverter? = null
        private var testStepValueExtractor: TestStepValueExtractor? = null
        private var getPropertyFunction: ((String) -> Any?)? = null

        fun withGetPropertyFunction(getPropertyFunction: ((String) -> Any?)?) =
            apply { this.getPropertyFunction = getPropertyFunction }

        fun withTestStepValueExtractor(testStepValueExtractor: TestStepValueExtractor?) =
            apply { this.testStepValueExtractor = testStepValueExtractor }

        fun withValueConverter(testStepValueConverter: TestStepValueConverter) = apply { this.testStepValueConverter = testStepValueConverter }

        fun build(): TestStepFactoryValueConverter {
            return TestStepFactoryValueConverter(
                testStepValueConverter?: error("TestStepValueConverter is mandatory"),
                testStepValueExtractor?: error("TestStepValueExtractor is mandatory"),
                getPropertyFunction)
        }

    }
}