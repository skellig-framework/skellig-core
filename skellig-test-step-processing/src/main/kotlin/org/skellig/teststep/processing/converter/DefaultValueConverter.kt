package org.skellig.teststep.processing.converter

import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.valueextractor.TestStepValueExtractor
import java.util.*
import java.util.function.Function

class DefaultValueConverter private constructor(val valueConverters: List<TestStepValueConverter>) : TestStepValueConverter {

    override fun convert(value: String?): Any? {
        var result: Any? = value
        for (valueConverter in valueConverters) {
            if (result != null && result.javaClass == String::class.java) {
                result = valueConverter.convert(result as String?)
            }
        }
        return result
    }

    class Builder {

        private val valueConverters = mutableListOf<TestStepValueConverter>()
        private var testScenarioState: TestScenarioState? = null
        private var testStepValueExtractor: TestStepValueExtractor? = null
        private var getPropertyFunction: Function<String, String?>? = null
        private var classLoader: ClassLoader? = null

        fun withTestScenarioState(testScenarioState: TestScenarioState?) = apply { this.testScenarioState = testScenarioState }

        fun withGetPropertyFunction(getPropertyFunction: Function<String, String?>?) = apply { this.getPropertyFunction = getPropertyFunction }

        fun withClassLoader(classLoader: ClassLoader?) = apply { this.classLoader = classLoader }

        fun withTestStepValueExtractor(testStepValueExtractor: TestStepValueExtractor?) = apply { this.testStepValueExtractor = testStepValueExtractor }

        fun withValueConverter(valueConverter: TestStepValueConverter) = apply { valueConverters.add(valueConverter) }

        fun build(): TestStepValueConverter {
            Objects.requireNonNull(testScenarioState, "Test Scenario State must be provided")

            valueConverters.add(0, PropertyValueConverter(valueConverters, getPropertyFunction))
            withValueConverter(TestStepStateValueConverter(testScenarioState!!, testStepValueExtractor))
            if (classLoader != null) {
                withValueConverter(FileValueConverter(classLoader!!))
            }
            withValueConverter(NumberValueConverter())
            withValueConverter(IncrementValueConverter())
            withValueConverter(DateTimeValueConverter())

            return DefaultValueConverter(valueConverters)
        }
    }
}