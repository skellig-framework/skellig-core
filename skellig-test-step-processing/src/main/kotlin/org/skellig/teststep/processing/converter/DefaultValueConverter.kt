package org.skellig.teststep.processing.converter

import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.valueextractor.TestStepValueExtractor
import java.util.*

class DefaultValueConverter private constructor(val valueConverters: List<TestStepValueConverter>) : TestStepValueConverter {

    override fun convert(value: String?): Any? {
        var result: Any? = value
        for (valueConverter in valueConverters) {
            if (result != null && result.javaClass == String::class.java) {
                result = valueConverter.convert(result as String?)
                //TODO: probably worth to use value extractors over here
            }
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
            if (classLoader != null) {
                withValueConverter(FileValueConverter(classLoader!!))
            }
            withValueConverter(NumberValueConverter())
            withValueConverter(IncrementValueConverter())
            withValueConverter(CurrentDateTimeValueConverter())
            withValueConverter(ToDateTimeValueConverter())
            withValueConverter(ListOfValueConverter())

            val defaultValueConverter = DefaultValueConverter(valueConverters)
            valueConverters.add(0, PropertyValueConverter(defaultValueConverter, getPropertyFunction))

            return defaultValueConverter
        }
    }
}