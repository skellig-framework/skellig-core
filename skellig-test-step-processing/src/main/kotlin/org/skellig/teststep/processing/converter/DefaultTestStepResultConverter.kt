package org.skellig.teststep.processing.converter

import org.skellig.teststep.processing.value.function.FunctionValueExecutor


class DefaultTestStepResultConverter private constructor(val testStepResultConverters: Map<String, TestStepResultConverter>) :
    TestStepResultConverter,
    FunctionValueExecutor {

    override fun execute(name: String, args: Array<Any?>): Any? {
        return (testStepResultConverters[name] as FunctionValueExecutor?)?.execute(name, args)
    }

    override fun getFunctionName(): String = ""

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