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

        private val functions = mutableMapOf<String, FunctionValueExecutor>()
        private var testScenarioState: TestScenarioState? = null
        private var classLoader: ClassLoader? = null
        private var classPaths: Collection<String>? = null

        fun withTestScenarioState(testScenarioState: TestScenarioState?) =
            apply { this.testScenarioState = testScenarioState }

        fun withClassLoader(classLoader: ClassLoader?) = apply { this.classLoader = classLoader }

        fun withClassPaths(classPaths: Collection<String>) = apply { this.classPaths = classPaths }

        fun withFunctionValueExecutor(functionValueExecutor: FunctionValueExecutor) =
            apply { functions[functionValueExecutor.getFunctionName()] = functionValueExecutor }

        fun build(): FunctionValueExecutor {
            Objects.requireNonNull(testScenarioState, "Test Scenario State must be provided")

            val defaultFunctionValueExecutor = DefaultFunctionValueExecutor(functions)

            this.withFunctionValueExecutor(IfFunctionExecutor())
            this.withFunctionValueExecutor(GetFromStateFunctionExecutor(testScenarioState!!))
            classLoader?.let {
                this.withFunctionValueExecutor(FileFunctionExecutor(it))
            }

            this.withFunctionValueExecutor(RandomFunctionExecutor())
            this.withFunctionValueExecutor(IncrementFunctionExecutor())
            this.withFunctionValueExecutor(CurrentDateTimeFunctionExecutor())
            this.withFunctionValueExecutor(ToDateTimeFunctionExecutor())
            this.withFunctionValueExecutor(ListOfFunctionExecutor())
            this.withFunctionValueExecutor(ToJsonFunctionExecutor())
            classLoader?.let {
                val fromCsvFunctionExecutor = FromCsvFunctionExecutor(it)
                this.withFunctionValueExecutor(fromCsvFunctionExecutor)
                this.withFunctionValueExecutor(FromCsvFunctionExecutor(it))
                this.withFunctionValueExecutor(FromTemplateFunctionExecutor(it))
                this.withFunctionValueExecutor(CustomFunctionExecutor(classPaths, it))
            }

            return defaultFunctionValueExecutor
        }
    }
}