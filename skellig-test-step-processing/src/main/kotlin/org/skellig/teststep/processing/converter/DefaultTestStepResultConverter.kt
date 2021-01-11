package org.skellig.teststep.processing.converter

class DefaultTestStepResultConverter private constructor(val testStepResultConverters: Map<String, TestStepResultConverter>) : TestStepResultConverter {

    override fun convert(convertFunction: String, result: Any?): Any? {
        return if (testStepResultConverters.containsKey(convertFunction)) {
            testStepResultConverters[convertFunction]?.convert(convertFunction, result)
        } else {
            result
        }
    }

    override fun getConvertFunctionName(): String {
        return ""
    }

    class Builder {

        private val testStepResultConverters = mutableMapOf<String, TestStepResultConverter>()

        fun withTestStepResultConverter(testStepResultConverter: TestStepResultConverter) =
                apply { testStepResultConverters[testStepResultConverter.getConvertFunctionName()] = testStepResultConverter }

        fun build(): TestStepResultConverter {
            testStepResultConverters["string"] = TestStepResultToStringConverter()
            return DefaultTestStepResultConverter(testStepResultConverters)
        }
    }
}