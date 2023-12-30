package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.exception.TestStepCreationException
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.MapValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression

abstract class BaseDefaultTestStepFactory<T : DefaultTestStep>(
    private val testStepRegistry: TestStepRegistry,
    valueExpressionContextFactory: ValueExpressionContextFactory,
    private val defaultTestDataConverter: String? = null,
) : BaseTestStepFactory<T>(valueExpressionContextFactory) {

    companion object {
        private val ID = AlphanumericValueExpression("id")

        private val PARENT_KEYWORD = AlphanumericValueExpression("parent")
        private val VALUES_KEYWORD = AlphanumericValueExpression("values")
        private val EXECUTION_KEYWORD = AlphanumericValueExpression("execution")
        private val TIMEOUT_KEYWORD = AlphanumericValueExpression("timeout")
        private val DELAY_KEYWORD = AlphanumericValueExpression("delay")
        private val ATTEMPTS_KEYWORD = AlphanumericValueExpression("attempts")
        private const val DEFAULT_TIMEOUT = 30000

        private var testDataKeywords: Set<ValueExpression>? = null
    }

    private var validationDetailsFactory = ValidationNodeFactory(valueExpressionContextFactory)
    private var stateUpdaterFactory = StateUpdaterFactory(valueExpressionContextFactory)
    private val testDataContext = valueExpressionContextFactory.create(emptyMap())

    init {
        testDataKeywords = setOf(
            AlphanumericValueExpression("data"),
            AlphanumericValueExpression("payload"),
            AlphanumericValueExpression("body"),
            AlphanumericValueExpression("request"),
            AlphanumericValueExpression("response"),
            AlphanumericValueExpression("message")
        )
    }

    override fun create(testStepName: String, rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, String?>): T {
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

    private fun createTestStep(testStepName: String, rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, String?>): T {
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

    protected abstract fun createTestStepBuilder(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): DefaultTestStep.Builder<T>

    protected open fun getStringArrayDataFromRawTestStep(
        propertyName: Any?,
        rawTestStep: Map<ValueExpression, ValueExpression?>,
        parameters: Map<String, Any?>
    ): Collection<String>? {
        return rawTestStep[propertyName]?.let {
            return when (val value = it.evaluate(valueExpressionContextFactory.create(parameters))) {
                is String -> listOf(value)
                is Collection<*> -> value.map { v -> v.toString() }.toList()
                is Array<*> -> value.map { v -> v.toString() }.toList()
                else -> null
            }
        }
    }

    protected open fun getTestDataKeywords(): Set<ValueExpression>? {
        return testDataKeywords
    }

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

    protected open fun extractTestData(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): Any? {
        return getTestDataKeywords()!!
            .find { rawTestStep.containsKey(it) }
            ?.let { convertTestData(rawTestStep[it], defaultTestDataConverter, parameters) }
    }

    private fun convertTestData(value: ValueExpression?, defaultTestDataConverter: String?, parameters: Map<String, Any?>): Any? {
        val convertedValue = convertValue<Any?>(value, parameters)
        return (defaultTestDataConverter?.let { testDataContext.onFunctionCall(it, null, arrayOf(convertedValue)) } ?: convertedValue)
    }

    protected open fun getId(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): String? {
        return if (rawTestStep.containsKey(ID)) convertValue<String>(rawTestStep[ID], parameters) else null
    }

    protected open fun getExecutionType(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): TestStepExecutionType? {
        return if (rawTestStep.containsKey(EXECUTION_KEYWORD)) {
            TestStepExecutionType.fromName(convertValue<String>(rawTestStep[EXECUTION_KEYWORD], parameters))
        } else TestStepExecutionType.SYNC
    }

    protected open fun getTimeout(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): Int {
        return getInteger(rawTestStep, parameters, TIMEOUT_KEYWORD, DEFAULT_TIMEOUT)
    }

    protected open fun getDelay(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): Int {
        return getInteger(rawTestStep, parameters, DELAY_KEYWORD, 0)
    }

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
                value = rawTimeout.toString().toInt()
            } catch (ignore: NumberFormatException) {
            }
        }
        return value
    }
}