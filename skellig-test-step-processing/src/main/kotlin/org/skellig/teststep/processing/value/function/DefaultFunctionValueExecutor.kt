package org.skellig.teststep.processing.value.function

import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.util.debug
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processing.value.function.collection.*
import java.util.*


/**
 * The main [FunctionValueExecutor] implementation that has registry of [FunctionValueExecutor] executes functions based on their names.
 * If function name is not found in the registry, there are 2 possible scenarios:
 * 1) If 'value' is provided in [DefaultFunctionValueExecutor.execute] method, then it will attempt to run the function as
 * a method of the 'value', if this method is found in the class of the 'value'. If the method is not found in the class of the 'value', then
 * it will use the function name as a property and will try to extract its data by available getter of the property, or
 * by key name if the 'value' is [Map].
 * 2) If 'value' is not provided in [DefaultFunctionValueExecutor.execute] method, then [CustomFunctionExecutor] is used
 * to find and execute functions registered through @[Function].
 *
 * @property functions A map of function names to their corresponding [FunctionValueExecutor] implementations.
 * Additional custom functions, configured via [FunctionsConfig][org.skellig.teststep.processing.value.config.FunctionsConfig],
 * will be added to this [Map].
 * @property customFunctionExecutor The [FunctionValueExecutor] implementation for custom functions. It has registry of
 * functions which are part of methods of classes marked @[Function].
 * @property objectValueFunctionExecutor The [FunctionValueExecutor] implementation for object value functions, used to
 * call as methods of a value or its properties.
 */
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

        private val log = logger<Builder>()

        private val functions = mutableMapOf<String, FunctionValueExecutor>()
        private var testScenarioState: TestScenarioState? = null
        private var classLoader: ClassLoader? = null
        private var classPaths: Collection<String>? = null
        private var classInstanceRegistry: MutableMap<Class<*>, Any>? = null

        fun withTestScenarioState(testScenarioState: TestScenarioState?) =
            apply { this.testScenarioState = testScenarioState }

        /**
         * Sets the class loader to be used for some internal Skellig Functions.
         */
        fun withClassLoader(classLoader: ClassLoader?) = apply { this.classLoader = classLoader }

        /**
         * Sets the class paths where the Framework can look for methods marked @[Function] to register
         * custom functions.
         */
        fun withClassPaths(classPaths: Collection<String>) = apply { this.classPaths = classPaths }

        /**
         * Registers a [FunctionValueExecutor] class for a specific function.
         *
         * @param functionValueExecutor the [FunctionValueExecutor] class to register
         */
        fun withFunctionValueExecutor(functionValueExecutor: FunctionValueExecutor) {
            log.debug {
                "Register function executor class '${functionValueExecutor::class.java}'" +
                        " for  function '${functionValueExecutor.getFunctionName()}'"
            }
            apply { functions[functionValueExecutor.getFunctionName()] = functionValueExecutor }
        }

        /**
         * Sets the class instance registry to be used for registration of custom functions marked with @[Function].
         * This registry keeps instances of classes instantiated earlier, so they can be reused elsewhere.
         * An example of where such classes can be instantiated are Test Step definition classes which may have test step
         * methods as well as methods marked as @[Function].
         */
        fun withClassInstanceRegistry(classInstanceRegistry: MutableMap<Class<*>, Any>) =
            apply { this.classInstanceRegistry = classInstanceRegistry }

        /**
         * Builds and returns an instance of the [DefaultFunctionValueExecutor] with all built-in Skellig Functions,
         * as well as all found custom functions in classes or registered through [FunctionsConfig][org.skellig.teststep.processing.value.config.FunctionsConfig].
         */
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
            this.withFunctionValueExecutor(ToInstantFunctionExecutor())
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