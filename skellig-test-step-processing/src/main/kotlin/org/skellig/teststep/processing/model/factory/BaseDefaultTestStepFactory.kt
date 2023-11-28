package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.exception.TestStepCreationException
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.validation.factory.ValidationNodeFactory
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.StringValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression

abstract class BaseDefaultTestStepFactory<T : DefaultTestStep>(
    private val testStepRegistry: TestStepRegistry,
    valueExpressionContextFactory: ValueExpressionContextFactory
) : BaseTestStepFactory<T>(valueExpressionContextFactory) {

    companion object {
        private val ID = StringValueExpression("id")

        private val PARENT_KEYWORD = StringValueExpression("parent")
        private val VARIABLES_KEYWORD = StringValueExpression("variables")
        private val EXECUTION_KEYWORD = StringValueExpression("execution")
        private val TIMEOUT_KEYWORD = StringValueExpression("timeout")
        private val DELAY_KEYWORD = StringValueExpression("delay")
        private val ATTEMPTS_KEYWORD = StringValueExpression("attempts")
        private const val DEFAULT_TIMEOUT = 30000

        private var testDataKeywords: Set<ValueExpression>? = null
    }

    private var validationDetailsFactory = ValidationNodeFactory(valueExpressionContextFactory)

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

        val variables = extractVariablesToParameters(rawTestStep, additionalParameters)

        return createTestStepBuilder(rawTestStep, additionalParameters)
            .withId(getId(rawTestStep, additionalParameters))
            .withName(testStepName)
            .withTestData(extractTestData(rawTestStep, additionalParameters))
            .withValidationDetails(validationDetailsFactory.create(rawTestStep, additionalParameters))
            .withVariables(variables)
            .withExecution(getExecutionType(rawTestStep, additionalParameters))
            .withTimeout(getTimeout(rawTestStep, additionalParameters))
            .withDelay(getDelay(rawTestStep, additionalParameters))
            .withAttempts(getAttempts(rawTestStep, additionalParameters))
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

    private fun extractVariablesToParameters(
        rawTestStep: Map<ValueExpression, ValueExpression?>,
        parameters: MutableMap<String, Any?>
    ): Map<String, Any?>? {
        val rawVariables = rawTestStep[VARIABLES_KEYWORD]
        return rawVariables?.let {
            (convertValue<Map<*,*>>(it, parameters) as Map<String, Any?>?)
                ?:throw TestStepCreationException("Variables of the test step must have key-value pair (ex. type: Map<String, Any>)")
        }
    }

    protected open fun extractTestData(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): Any? {
        return getTestDataKeywords()!!
            .filter { rawTestStep.containsKey(it) }
            .map { convertValue<Any>(rawTestStep[it], parameters) }
            .firstOrNull()
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
        return getInteger(rawTestStep, parameters,DELAY_KEYWORD, 0)
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