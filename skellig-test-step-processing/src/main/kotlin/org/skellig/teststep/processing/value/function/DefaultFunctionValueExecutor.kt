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

        fun withTestScenarioState(testScenarioState: TestScenarioState?) =
            apply { this.testScenarioState = testScenarioState }

        fun withClassLoader(classLoader: ClassLoader?) = apply { this.classLoader = classLoader }

        fun withClassPaths(classPaths: Collection<String>) = apply { this.classPaths = classPaths }

        fun withFunctionValueExecutor(functionValueExecutor: FunctionValueExecutor) =
            apply { functions[functionValueExecutor.getFunctionName()] = functionValueExecutor }

        fun build(): FunctionValueExecutor {
            Objects.requireNonNull(testScenarioState, "Test Scenario State must be provided")

            this.withFunctionValueExecutor(JsonPathValueExtractor())
            this.withFunctionValueExecutor(JsonToMapTestStepValueExtractor())
            this.withFunctionValueExecutor(JsonToListTestStepValueExtractor())
            this.withFunctionValueExecutor(XPathValueExtractor())
            this.withFunctionValueExecutor(GetValuesFunctionExecutor())
            this.withFunctionValueExecutor(FromIndexValueExtractor())
            this.withFunctionValueExecutor(ToStringValueExtractor())
            this.withFunctionValueExecutor(SubStringValueExtractor())
            this.withFunctionValueExecutor(SubStringLastValueExtractor())
            this.withFunctionValueExecutor(ToIntTestStepValueExtractor())
            this.withFunctionValueExecutor(ToLongTestStepValueExtractor())
            this.withFunctionValueExecutor(FromRegexValueExtractor())
            this.withFunctionValueExecutor(ToByteTestStepValueExtractor())
            this.withFunctionValueExecutor(ToShortTestStepValueExtractor())
            this.withFunctionValueExecutor(ToFloatTestStepValueExtractor())
            this.withFunctionValueExecutor(ToDoubleTestStepValueExtractor())
            this.withFunctionValueExecutor(ToBooleanTestStepValueExtractor())
            this.withFunctionValueExecutor(ToBigDecimalTestStepValueExtractor())
            this.withFunctionValueExecutor(ToDateTimeValueExtractor())
            this.withFunctionValueExecutor(ToDateValueExtractor())
            this.withFunctionValueExecutor(ToBytesValueExtractor())
            this.withFunctionValueExecutor(SizeValueExtractor())
            this.withFunctionValueExecutor(AllFunctionExecutor())
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
            this.withFunctionValueExecutor(ToDateTimeFunctionExecutor())
            this.withFunctionValueExecutor(ListOfFunctionExecutor())
            this.withFunctionValueExecutor(ToJsonFunctionExecutor())
            this.withFunctionValueExecutor(ContainsValueComparator())
            this.withFunctionValueExecutor(MatchValueComparator())
            this.withFunctionValueExecutor(DateTimeValueComparator())
            classLoader?.let {
                val fromCsvFunctionExecutor = FromCsvFunctionExecutor(it)
                this.withFunctionValueExecutor(fromCsvFunctionExecutor)
                this.withFunctionValueExecutor(FileFunctionExecutor(it))
                this.withFunctionValueExecutor(FromCsvFunctionExecutor(it))
                this.withFunctionValueExecutor(FromTemplateFunctionExecutor(it))
            }

            return DefaultFunctionValueExecutor(functions, CustomFunctionExecutor(classPaths), NewObjectValueExtractor())
        }
    }
}