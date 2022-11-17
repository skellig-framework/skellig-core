package org.skellig.teststep.processing.converter

import org.skellig.teststep.processing.experiment.FunctionValueProcessor
import org.skellig.teststep.processing.experiment.ValueExtractor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.valueextractor.TestStepValueExtractor
import java.util.*

class DefaultValueConverter private constructor(private val functions: Map<String, FunctionValueProcessor>) : FunctionValueProcessor {

    override fun execute(name: String, args: Array<Any?>): Any? = functions[name]?.execute(name, args)

    override fun getFunctionName(): String = ""

    class Builder {

        private val valueConverters = mutableListOf<TestStepValueConverter>()
        private val functions = mutableMapOf<String, FunctionValueProcessor>()
        private var testScenarioState: TestScenarioState? = null
        private var valueExtractor: ValueExtractor? = null
        private var getPropertyFunction: ((String) -> Any?)? = null
        private var classLoader: ClassLoader? = null
        private var classPaths: Collection<String>? = null

        fun withTestScenarioState(testScenarioState: TestScenarioState?) =
            apply { this.testScenarioState = testScenarioState }

        fun withGetPropertyFunction(getPropertyFunction: ((String) -> Any?)?) =
            apply { this.getPropertyFunction = getPropertyFunction }

        fun withClassLoader(classLoader: ClassLoader?) = apply { this.classLoader = classLoader }

        fun withClassPaths(classPaths: Collection<String>) = apply { this.classPaths = classPaths }

        fun withTestStepValueExtractor(valueExtractor: ValueExtractor?) =
            apply { this.valueExtractor = valueExtractor }

        fun withValueConverter(valueConverter: TestStepValueConverter) = apply { valueConverters.add(valueConverter) }

        fun withFunctionProcessor(functionProcessor: FunctionValueProcessor) = apply { functions[functionProcessor.getFunctionName()] = functionProcessor }

        fun build(): FunctionValueProcessor {
            Objects.requireNonNull(testScenarioState, "Test Scenario State must be provided")

            val defaultValueConverter = DefaultValueConverter(functions)

            withFunctionProcessor(TestDataFromIfStatementConverter())
            withFunctionProcessor(TestStepStateValueConverter(testScenarioState!!))
//            withFunctionProcessor(FindFromStateValueConverter(testScenarioState!!, testStepValueExtractor))
            classLoader?.let {
                withFunctionProcessor(FileValueConverter(it))
            }

            withFunctionProcessor(RandomValueConverter())
            withFunctionProcessor(IncrementValueConverter())
            withFunctionProcessor(CurrentDateTimeValueConverter())
            withFunctionProcessor(ToDateTimeValueConverter())
            withFunctionProcessor(ListOfValueConverter())
            withFunctionProcessor(TestDataToJsonConverter())
            classLoader?.let {
                val testDataFromCsvConverter = TestDataFromCsvConverter(it)
               withFunctionProcessor(testDataFromCsvConverter)
               withFunctionProcessor(TestDataFromCsvConverter(it))
               withFunctionProcessor(TestDataFromFTLConverter(it, testDataFromCsvConverter))
               withFunctionProcessor(CustomFunctionValueConverter(classPaths, it))
            }

            return defaultValueConverter
        }
    }
}