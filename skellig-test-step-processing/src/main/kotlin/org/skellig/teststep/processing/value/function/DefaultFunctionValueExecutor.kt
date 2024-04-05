package org.skellig.teststep.processing.value.function

import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.value.function.collection.*
import java.util.*

class DefaultFunctionValueExecutor private constructor(
    private val functions: Map<String, FunctionValueExecutor>,
    private val customFunctionExecutor: FunctionValueExecutor,
    private val objectValueFunctionExecutor: FunctionValueExecutor,
) : FunctionValueExecutor {

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any? {
        val executor = if (functions.containsKey(name)) functions[name]
        else if (value != null) objectValueFunctionExecutor
        // if no function found, then try the custom one if registered.
        else customFunctionExecutor

        return executor?.execute(name, value, args)
    }

    override fun getFunctionName(): String = ""

    class Builder {

        private val functions = mutableMapOf<String, FunctionValueExecutor>()
        private var testScenarioState: TestScenarioState? = null
        private var classLoader: ClassLoader? = null
        private var classPaths: Collection<String>? = null
        private var classInstanceRegistry: MutableMap<Class<*>, Any>? = null

        fun withTestScenarioState(testScenarioState: TestScenarioState?) =
            apply { this.testScenarioState = testScenarioState }

        fun withClassLoader(classLoader: ClassLoader?) = apply { this.classLoader = classLoader }

        fun withClassPaths(classPaths: Collection<String>) = apply { this.classPaths = classPaths }

        fun withFunctionValueExecutor(functionValueExecutor: FunctionValueExecutor) =
            apply { functions[functionValueExecutor.getFunctionName()] = functionValueExecutor }

        fun withClassInstanceRegistry(classInstanceRegistry: MutableMap<Class<*>, Any>) =
            apply { this.classInstanceRegistry = classInstanceRegistry }

        fun build(): FunctionValueExecutor {
            Objects.requireNonNull(testScenarioState, "Test Scenario State must be provided")

            this.withFunctionValueExecutor(JsonPathFunctionExecutor())
            this.withFunctionValueExecutor(JsonToMapTestStepFunctionExecutor())
            this.withFunctionValueExecutor(JsonToListTestStepFunctionExecutor())
            this.withFunctionValueExecutor(XPathFunctionExecutor())
            this.withFunctionValueExecutor(GetValuesFunctionExecutor())
            this.withFunctionValueExecutor(FirstFunctionExecutor())
            this.withFunctionValueExecutor(FromIndexFunctionExecutor())
            this.withFunctionValueExecutor(ToStringFunctionExecutor())
            this.withFunctionValueExecutor(SubStringFunctionExecutor())
            this.withFunctionValueExecutor(SubStringLastFunctionExecutor())
            this.withFunctionValueExecutor(ToIntFunctionExecutor())
            this.withFunctionValueExecutor(ToLongFunctionExecutor())
            this.withFunctionValueExecutor(FromRegexFunctionExecutor())
            this.withFunctionValueExecutor(ToByteFunctionExecutor())
            this.withFunctionValueExecutor(ToShortFunctionExecutor())
            this.withFunctionValueExecutor(ToFloatFunctionExecutor())
            this.withFunctionValueExecutor(ToDoubleFunctionExecutor())
            this.withFunctionValueExecutor(ToBooleanFunctionExecutor())
            this.withFunctionValueExecutor(ToBigDecimalFunctionExecutor())
            this.withFunctionValueExecutor(ToDateTimeFunctionExecutor())
            this.withFunctionValueExecutor(ToDateFunctionExecutor())
            this.withFunctionValueExecutor(ToBytesFunctionExecutor())
            this.withFunctionValueExecutor(SizeFunctionExecutor())
            this.withFunctionValueExecutor(AllFunctionExecutor())
            this.withFunctionValueExecutor(AddFunctionExecutor())
            this.withFunctionValueExecutor(AnyFunctionExecutor())
            this.withFunctionValueExecutor(NoneFunctionExecutor())
            this.withFunctionValueExecutor(CountFunctionExecutor())
            this.withFunctionValueExecutor(FindAllFunctionExecutor())
            this.withFunctionValueExecutor(FindFunctionExecutor())
            this.withFunctionValueExecutor(FindLastFunctionExecutor())
            this.withFunctionValueExecutor(GroupByFunctionExecutor())
            this.withFunctionValueExecutor(MapFunctionExecutor())
            this.withFunctionValueExecutor(MinOfFunctionExecutor())
            this.withFunctionValueExecutor(MaxOfFunctionExecutor())
            this.withFunctionValueExecutor(SortFunctionExecutor())
            this.withFunctionValueExecutor(SumOfFunctionExecutor())
            this.withFunctionValueExecutor(IfFunctionExecutor())
            this.withFunctionValueExecutor(GetFromStateFunctionExecutor(testScenarioState!!))
            this.withFunctionValueExecutor(RandomFunctionExecutor())
            this.withFunctionValueExecutor(IncrementFunctionExecutor())
            this.withFunctionValueExecutor(CurrentDateTimeFunctionExecutor())
            this.withFunctionValueExecutor(ListOfFunctionExecutor())
            this.withFunctionValueExecutor(ToJsonFunctionExecutor())
            this.withFunctionValueExecutor(ContainsFunctionExecutor())
            this.withFunctionValueExecutor(MatchValueFunctionExecutor())
            this.withFunctionValueExecutor(DateTimeCompareFunctionExecutor())
            classLoader?.let {
                val fromCsvFunctionExecutor = FromCsvFunctionExecutor(it)
                this.withFunctionValueExecutor(fromCsvFunctionExecutor)
                this.withFunctionValueExecutor(FileFunctionExecutor(it))
                this.withFunctionValueExecutor(FromCsvFunctionExecutor(it))
                this.withFunctionValueExecutor(FromTemplateFunctionExecutor(it))
            }

            return DefaultFunctionValueExecutor(
                functions,
                CustomFunctionExecutor(classPaths, classInstanceRegistry ?: mutableMapOf()),
                FromObjectFunctionExecutor()
            )
        }
    }
}