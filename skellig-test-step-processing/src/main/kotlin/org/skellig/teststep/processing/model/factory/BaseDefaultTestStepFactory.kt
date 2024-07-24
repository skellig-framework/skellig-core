package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.exception.TestStepCreationException
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.reader.value.expression.MapValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression

/**
 * Abstract class for creating test step factories for test steps and must provide implementation for creating
 * specific test steps of [DefaultTestStep].
 *
 * @param T the type of [DefaultTestStep] that the factory creates
 * @property testStepRegistry the registry of test steps
 * @property valueExpressionContextFactory the factory for creating value expression contexts for evaluation of [ValueExpression]
 * @property defaultTestDataConverter the name of default converter (function) for test data (optional)
 */
abstract class BaseDefaultTestStepFactory<T : DefaultTestStep>(
    protected val testStepRegistry: TestStepRegistry,
    valueExpressionContextFactory: ValueExpressionContextFactory,
    protected val defaultTestDataConverter: String? = null,
) : BaseTestStepFactory<T>(valueExpressionContextFactory) {

    companion object {
        private val ID = fromProperty("id")
        private val PARENT_KEYWORD = fromProperty("parent")
        private val VALUES_KEYWORD = fromProperty("values")
        private val EXECUTION_KEYWORD = fromProperty("execution")
        private val TIMEOUT_KEYWORD = fromProperty("timeout")
        private val DELAY_KEYWORD = fromProperty("delay")
        private val ATTEMPTS_KEYWORD = fromProperty("attempts")
        private const val DEFAULT_TIMEOUT = 30000

        private var testDataKeywords = setOf(
            fromProperty("data"),
            fromProperty("payload"),
            fromProperty("body"),
            fromProperty("request"),
            fromProperty("response"),
            fromProperty("message")
        )
    }

    private var validationDetailsFactory = ValidationNodeFactory(valueExpressionContextFactory)
    private var stateUpdaterFactory = StateUpdaterFactory(valueExpressionContextFactory)
    private val testDataContext = valueExpressionContextFactory.create(emptyMap())

    override fun create(testStepName: String, rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): T {
        val parentTestSteps = rawTestStep[PARENT_KEYWORD]
        return if (parentTestSteps != null) {
            val newRawTestStep = mutableMapOf<ValueExpression, ValueExpression?>()
            // if parent exists, then merge its data with rawTestStep
            when (val evaluateParentTestSteps = parentTestSteps.evaluate(valueExpressionContextFactory.create(parameters))) {
                is String -> testStepRegistry.getById(evaluateParentTestSteps)?.let { newRawTestStep.putAll(it) }
                is Collection<*> -> evaluateParentTestSteps.forEach { parentTestStep ->
                    testStepRegistry.getById(parentTestStep as String)?.let { newRawTestStep.putAll(it) }
                }
            }

            newRawTestStep.putAll(rawTestStep)
            createTestStep(testStepName, newRawTestStep, parameters)
        } else createTestStep(testStepName, rawTestStep, parameters)
    }

    private fun createTestStep(testStepName: String, rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): T {
        val additionalParameters = HashMap<String, Any?>(parameters)
        val parametersFromTestName = extractParametersFromTestStepName(testStepName, rawTestStep)
        parametersFromTestName?.let {
            additionalParameters.putAll(parametersFromTestName)
        }

        val values = extractValuesToParameters(rawTestStep, additionalParameters)
        values?.let { additionalParameters.putAll(it) }

        return createTestStepBuilder(rawTestStep, additionalParameters)
            .withId(getId(rawTestStep, additionalParameters))
            .withName(testStepName)
            .withTestData(extractTestData(rawTestStep, additionalParameters))
            .withValidationDetails(validationDetailsFactory.create(rawTestStep, additionalParameters))
            .withValues(values)
            .withExecution(getExecutionType(rawTestStep, additionalParameters))
            .withTimeout(getTimeout(rawTestStep, additionalParameters))
            .withDelay(getDelay(rawTestStep, additionalParameters))
            .withAttempts(getAttempts(rawTestStep, additionalParameters))
            .withScenarioStateUpdater(stateUpdaterFactory.create(rawTestStep, additionalParameters))
            .build()
    }

    /**
     * Creates a builder for a test step. The builder must extend [DefaultTestStep.Builder] and is needed to
     * set up its specific dependencies inside an extending class.
     * Later this builder is used by [BaseDefaultTestStepFactory] to set up the remaining properties and dependencies.
     *
     * @param rawTestStep The raw representation of the test step.
     * @param parameters The parameters to be used in constructing the test step builder.
     * @return The builder for the test step.
     */
    protected abstract fun createTestStepBuilder(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): DefaultTestStep.Builder<T>

    /**
     * Retrieves a collection of strings from the given raw test step based on the provided property name.
     * The collection is generated by evaluating the value expressions in the raw test step using the provided parameters.
     * If the value of the property is a String, a single-element list containing the string is returned.
     * If the value is a Collection or Array, each element is converted to a string and added to the result list.
     * If the value expression evaluates to any other type, null is returned.
     *
     * @param propertyName The property name used to retrieve the value from the raw test step.
     * @param rawTestStep The raw test step containing the property and its value expression.
     * @param parameters The parameters used in evaluating the value expression.
     * @return A collection of strings or null if the value is of a different type.
     */
    protected open fun getStringArrayDataFromRawTestStep(
        propertyName: Any?,
        rawTestStep: Map<ValueExpression, ValueExpression?>,
        parameters: Map<String, Any?>
    ): Collection<String>? {
        return rawTestStep[propertyName]?.let {
            val context = valueExpressionContextFactory.create(parameters)
            return when (val value = it.evaluate(context)) {
                is String -> listOf(value)
                is Collection<*> -> value.map { v -> v.toString() }.toList()
                is Array<*> -> value.map { v -> v.toString() }.toList()
                else -> null
            }
        }
    }

    /**
     * Get the set of keywords for test data.
     *
     * @return Set of keywords for test data
     */
    protected open fun getTestDataKeywords(): Set<ValueExpression> {
        return testDataKeywords
    }

    /**
     * Extracts values from the raw test step.
     *
     * @param rawTestStep The raw representation of the test step.
     * @param parameters The parameters to be used in converting the values. It adds each value from 'values' to the parameters,
     * as they may be used as reference in other value definition.
     * @return A map containing the extracted and converted values, or null if the raw test step does not contain any values.
     * @throws TestStepCreationException If the values of the test step do not have key-value pairs.
     */
    private fun extractValuesToParameters(
        rawTestStep: Map<ValueExpression, ValueExpression?>,
        parameters: MutableMap<String, Any?>
    ): Map<String, Any?>? {
        val rawValues = rawTestStep[VALUES_KEYWORD]
        return rawValues?.let {
            (rawValues as? MapValueExpression)?.value?.mapNotNull { entry ->
                val convertedKey = convertValue<String>(entry.key, parameters)
                convertedKey?.let {
                    val convertedVar = convertValue<Any>(entry.value, parameters)
                    parameters[convertedKey] = convertedVar
                    convertedKey to convertedVar
                }
            }?.toMap() ?: throw TestStepCreationException("Values of the test step must have key-value pair (ex. type: Map<String, Any>)")
        }
    }

    /**
     * Extracts the test data from the raw test step by one of the provided keywords [BaseDefaultTestStepFactory.getTestDataKeywords]
     * and converts it using the defaultTestDataConverter (if provided) and parameters.
     *
     * @param rawTestStep The raw representation of the test step.
     * @param parameters The parameters used in evaluating the test data.
     *
     * @return The extracted and converted test data, or null if the raw test step does not contain any test data.
     */
    protected open fun extractTestData(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): Any? {
        return getTestDataKeywords()
            .find { rawTestStep.containsKey(it) }
            ?.let { convertTestData(rawTestStep[it], defaultTestDataConverter, parameters) }
    }

    private fun convertTestData(value: ValueExpression?, defaultTestDataConverter: String?, parameters: Map<String, Any?>): Any? {
        val convertedValue = convertValue<Any?>(value, parameters)
        return (defaultTestDataConverter?.let { testDataContext.onFunctionCall(it, null, arrayOf(convertedValue)) } ?: convertedValue)
    }

    /**
     * Retrieves the ID value from the raw test step.
     *
     * @param rawTestStep The raw test step map.
     * @param parameters The parameters used in evaluating the ID value.
     * @return The ID value if it is present in the raw test step, otherwise null.
     */
    protected open fun getId(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): String? {
        return if (rawTestStep.containsKey(ID)) convertValue<String>(rawTestStep[ID], parameters) else null
    }

    /**
     * Retrieves the [TestStepExecutionType] from the raw test step.
     *
     * @param rawTestStep The raw representation of the test step.
     * @param parameters The parameters used in evaluating the execution type.
     * @return The execution type of the test step.
     */
    protected open fun getExecutionType(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): TestStepExecutionType {
        return if (rawTestStep.containsKey(EXECUTION_KEYWORD)) {
            TestStepExecutionType.fromName(convertValue<String>(rawTestStep[EXECUTION_KEYWORD], parameters))
        } else TestStepExecutionType.SYNC
    }

    /**
     * Retrieves the timeout value from the raw test step.
     * If not defined, then default timeout = 30000 is used.
     *
     * @param rawTestStep The raw representation of the test step.
     * @param parameters The parameters used in evaluating the timeout value.
     * @return The timeout value.
     */
    protected open fun getTimeout(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): Int {
        return getInteger(rawTestStep, parameters, TIMEOUT_KEYWORD, DEFAULT_TIMEOUT)
    }

    /**
     * Retrieves the delay value from the raw test step.
     * If the value is not present in the raw test step, the default value of 0 is used.
     *
     * @param rawTestStep The raw representation of the test step.
     * @param parameters The parameters to be used in evaluating the delay value.
     * @return The delay value.
     */
    protected open fun getDelay(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): Int {
        return getInteger(rawTestStep, parameters, DELAY_KEYWORD, 0)
    }

    /**
     * Retrieves the number of attempts for the test step from the raw test step.
     * If the value is not present in the raw test step, the default value of 0 is used.
     *
     * @param rawTestStep The raw test step map.
     * @param parameters The parameters used in evaluating the value expression.
     * @return The number of attempts.
     */
    protected open fun getAttempts(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): Int {
        return getInteger(rawTestStep, parameters, ATTEMPTS_KEYWORD, 0)
    }

    private fun getInteger(
        rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>,
        keywordName: ValueExpression, defaultValue: Int
    ): Int {
        var value = defaultValue
        if (rawTestStep.containsKey(keywordName)) {
            val rawTimeout = convertValue<Any>(rawTestStep[keywordName], parameters)
            try {
                value = rawTimeout?.toString()?.toInt() ?: defaultValue
            } catch (ignore: NumberFormatException) {
            }
        }
        return value
    }
}