package org.skellig.teststep.processing.converter

import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.valueextractor.TestStepValueExtractor
import java.util.*

class DefaultValueConverter private constructor(val valueConverters: List<TestStepValueConverter>) : TestStepValueConverter {

    override fun convert(value: Any?): Any? {
        val newValue = when (value) {
            is Map<*, *> -> value.entries.map { it.key to convert(it.value) }.toMap()
            is Collection<*> -> value.map { convert(it) }.toList()
            else -> convertWithConverters(value)
        }
        // If collection then go through conversion again to avoid missed function processing
        return if (newValue is Map<*, *> || newValue is Collection<*>) convertWithConverters(newValue)
        else newValue
    }

    private fun convertWithConverters(value: Any?): Any? {
        var result: Any? = value
        for (valueConverter in valueConverters) {
            result = valueConverter.convert(result)
            //TODO: probably worth to use value extractors over here
        }
        return result
    }

    class Builder {

        private val valueConverters = mutableListOf<TestStepValueConverter>()
        private var testScenarioState: TestScenarioState? = null
        private var testStepValueExtractor: TestStepValueExtractor? = null
        private var getPropertyFunction: ((String) -> String?)? = null
        private var classLoader: ClassLoader? = null

        fun withTestScenarioState(testScenarioState: TestScenarioState?) = apply { this.testScenarioState = testScenarioState }

        fun withGetPropertyFunction(getPropertyFunction: ((String) -> String?)?) = apply { this.getPropertyFunction = getPropertyFunction }

        fun withClassLoader(classLoader: ClassLoader?) = apply { this.classLoader = classLoader }

        fun withTestStepValueExtractor(testStepValueExtractor: TestStepValueExtractor?) = apply { this.testStepValueExtractor = testStepValueExtractor }

        fun withValueConverter(valueConverter: TestStepValueConverter) = apply { valueConverters.add(valueConverter) }

        fun build(): TestStepValueConverter {
            Objects.requireNonNull(testScenarioState, "Test Scenario State must be provided")

            withValueConverter(TestStepStateValueConverter(testScenarioState!!, testStepValueExtractor))
            withValueConverter(FindFromStateValueConverter(testScenarioState!!, testStepValueExtractor))
            classLoader?.let {
                withValueConverter(FileValueConverter(it))
            }

            withValueConverter(NumberValueConverter())
            withValueConverter(IncrementValueConverter())
            withValueConverter(CurrentDateTimeValueConverter())
            withValueConverter(ToDateTimeValueConverter())
            withValueConverter(ListOfValueConverter())
            withValueConverter(TestDataToBytesConverter())
            withValueConverter(TestDataToJsonConverter())
            classLoader?.let {
                val testDataFromCsvConverter = TestDataFromCsvConverter(it)
                withValueConverter(testDataFromCsvConverter)
                withValueConverter(TestDataFromCsvConverter(it))
                withValueConverter(TestDataFromFTLConverter(it, testDataFromCsvConverter))
            }

            val defaultValueConverter = DefaultValueConverter(valueConverters)
            valueConverters.add(0, TestDataFromIfStatementConverter())
            valueConverters.add(1, PropertyValueConverter(defaultValueConverter, getPropertyFunction))

            return defaultValueConverter
        }
    }
}