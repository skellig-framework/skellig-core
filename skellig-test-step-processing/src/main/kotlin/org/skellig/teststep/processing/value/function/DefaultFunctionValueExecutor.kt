package org.skellig.teststep.processing.value.function

import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.value.extractor.ValueExtractor
import java.util.*

class DefaultFunctionValueExecutor private constructor(private val functions: Map<String, FunctionValueExecutor>) : FunctionValueExecutor {

    override fun execute(name: String, args: Array<Any?>): Any? {
        return if (functions.containsKey(name)) functions[name]?.execute(name, args)
        else functions[""]?.execute(name, args) // if no function found, then try the custom one if registered.
    }

    override fun getFunctionName(): String = ""

    class Builder {

        private val functionValueExecutors = mutableListOf<FunctionValueExecutor>()
        private val functions = mutableMapOf<String, FunctionValueExecutor>()
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

        fun withFunctionValueExecutor(functionValueExecutor: FunctionValueExecutor) = apply { functionValueExecutors.add(functionValueExecutor) }

        fun withFunctionProcessor(functionProcessor: FunctionValueExecutor) = apply { functions[functionProcessor.getFunctionName()] = functionProcessor }

        fun build(): FunctionValueExecutor {
            Objects.requireNonNull(testScenarioState, "Test Scenario State must be provided")

            val defaultFunctionValueExecutor = DefaultFunctionValueExecutor(functions)

            withFunctionProcessor(IfFunctionExecutor())
            withFunctionProcessor(GetFromStateFunctionExecutor(testScenarioState!!))
            classLoader?.let {
                withFunctionProcessor(FileFunctionExecutor(it))
            }

            withFunctionProcessor(RandomFunctionExecutor())
            withFunctionProcessor(IncrementFunctionExecutor())
            withFunctionProcessor(CurrentDateTimeFunctionExecutor())
            withFunctionProcessor(ToDateTimeFunctionExecutor())
            withFunctionProcessor(ListOfFunctionExecutor())
            withFunctionProcessor(ToJsonFunctionExecutor())
            classLoader?.let {
                val fromCsvFunctionExecutor = FromCsvFunctionExecutor(it)
                withFunctionProcessor(fromCsvFunctionExecutor)
                withFunctionProcessor(FromCsvFunctionExecutor(it))
                withFunctionProcessor(FromTemplateFunctionExecutor(it))
                withFunctionProcessor(CustomFunctionExecutor(classPaths, it))
            }

            return defaultFunctionValueExecutor
        }
    }
}