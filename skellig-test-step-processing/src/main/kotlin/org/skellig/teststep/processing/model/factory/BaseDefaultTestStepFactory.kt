package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.exception.TestStepCreationException
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.TestStepExecutionType
import java.util.*
import java.util.regex.Pattern

abstract class BaseDefaultTestStepFactory<T : DefaultTestStep>(
        private val testStepRegistry: TestStepRegistry,
        keywordsProperties: Properties?,
        testStepFactoryValueConverter: TestStepFactoryValueConverter)
    : BaseTestStepFactory<T>(keywordsProperties, testStepFactoryValueConverter) {

    companion object {
        private val COMMA_SPLIT_PATTERN = Pattern.compile(",")

        private const val PARENT_KEYWORD = "test.step.keyword.parent"
        private const val VARIABLES_KEYWORD = "test.step.keyword.variables"
        private const val EXECUTION_KEYWORD = "test.step.keyword.execution"
        private const val TIMEOUT_KEYWORD = "test.step.keyword.timeout"
        private const val ATTEMPTS_KEYWORD = "test.step.keyword.attempts"
        private const val DELAY_KEYWORD = "test.step.keyword.delay"
        private const val DEFAULT_TIMEOUT = 30000

        private var testDataKeywords: Set<String>? = null
    }

    private var validationDetailsFactory = ValidationDetailsFactory(keywordsProperties, testStepFactoryValueConverter)

    init {
        testDataKeywords = setOf(
                getKeywordName("test.step.keyword.data", "data"),
                getKeywordName("test.step.keyword.payload", "payload"),
                getKeywordName("test.step.keyword.body", "body"),
                getKeywordName("test.step.keyword.request", "request"),
                getKeywordName("test.step.keyword.response", "response"),
                getKeywordName("test.step.keyword.message", "message"))
    }

    override fun create(testStepName: String, rawTestStep: Map<String, Any?>, parameters: Map<String, String?>): T {
        val parentTestSteps = rawTestStep[getKeywordName(PARENT_KEYWORD, "parent")]
        return if (parentTestSteps != null) {
            val newRawTestStep = mutableMapOf<String, Any?>()
            // if parent exists, then merge its data with rawTestStep
            when(parentTestSteps) {
                is String -> testStepRegistry.getById(parentTestSteps)?.let { newRawTestStep.putAll(it) }
                is Collection<*> -> parentTestSteps.forEach{ parentTestStep ->
                    testStepRegistry.getById(parentTestStep as String)?.let { newRawTestStep.putAll(it) }
                }
            }

            newRawTestStep.putAll(rawTestStep)
            createTestStep(testStepName, newRawTestStep, parameters)
        } else createTestStep(testStepName, rawTestStep, parameters)
    }

    fun createTestStep(testStepName: String, rawTestStep: Map<String, Any?>, parameters: Map<String, String?>): T {
        val additionalParameters: MutableMap<String, Any?> = HashMap(parameters)
        val parametersFromTestName = extractParametersFromTestStepName(testStepName, rawTestStep)
        parametersFromTestName?.let {
            additionalParameters.putAll(parametersFromTestName)
        }

        val variables = extractVariablesToParameters(rawTestStep, additionalParameters)

        return createTestStepBuilder(rawTestStep, additionalParameters)
            .withId(getId(rawTestStep, additionalParameters))
            .withName(convertValue<String>(testStepName, additionalParameters))
            .withTestData(extractTestData(rawTestStep, additionalParameters))
            .withValidationDetails(validationDetailsFactory.create(rawTestStep, additionalParameters))
            .withVariables(variables)
            .withExecution(getExecutionType(rawTestStep))
            .withTimeout(getTimeout(rawTestStep, additionalParameters))
            .withDelay(getDelay(rawTestStep, additionalParameters))
            .withAttempts(getAttempts(rawTestStep, additionalParameters))
            .build()
    }

    protected abstract fun createTestStepBuilder(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): DefaultTestStep.Builder<T>

    protected open fun getStringArrayDataFromRawTestStep(propertyName: String?, rawTestStep: Map<String, Any?>,
                                                         parameters: Map<String, Any?>): Collection<String>? {
        return rawTestStep[propertyName]?.let {
            when {
                it is String -> {
                    return COMMA_SPLIT_PATTERN.split(convertValue(it, parameters)).toList()
                }
                it is Collection<*> -> return it as Collection<String>
                it.javaClass.isArray -> return (it as Array<String>).toList()
                else -> null
            }
        }
    }

    protected open fun getTestDataKeywords(): Set<String>? {
        return testDataKeywords
    }

    private fun extractVariablesToParameters(
        rawTestStep: Map<String, Any?>,
        parameters: MutableMap<String, Any?>
    ): Map<String, Any?>? {
        val rawVariables = rawTestStep[getKeywordName(VARIABLES_KEYWORD, "variables")]
        return rawVariables?.let {
            if (rawVariables is Map<*, *>) {
                rawVariables.map {
                    val convertedVar = convertValue<Any>(it.value, parameters)
                    parameters[it.key.toString()] = convertedVar
                    it.key.toString() to convertedVar
                }.toMap()
            } else throw TestStepCreationException("variables of the test step must have key-value pair (ex. type: Map<*, *>)")
        }
    }

    private fun extractTestData(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): Any? {
        return getTestDataKeywords()!!
                .filter { rawTestStep.containsKey(it) }
                .map { keyword: String -> convertValue<Any>(rawTestStep[keyword], parameters) }
                .firstOrNull()
    }

    protected open fun getId(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): String? {
        return if (rawTestStep.containsKey("id")) convertValue<String>(rawTestStep["id"], parameters) else null
    }

    protected open fun getExecutionType(rawTestStep: Map<String, Any?>): TestStepExecutionType? {
        val executionKeyword = getKeywordName(EXECUTION_KEYWORD, "execution")
        return if (rawTestStep.containsKey(executionKeyword)) {
            TestStepExecutionType.fromName(rawTestStep[executionKeyword].toString())
        } else TestStepExecutionType.SYNC
    }

    protected open fun getTimeout(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): Int {
        return getInteger(rawTestStep, parameters, getKeywordName(TIMEOUT_KEYWORD, "timeout"), DEFAULT_TIMEOUT)
    }

    protected open fun getDelay(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): Int {
        return getInteger(rawTestStep, parameters, getKeywordName(DELAY_KEYWORD, "delay"), 0)
    }

    protected open fun getAttempts(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): Int {
        return getInteger(rawTestStep, parameters, getKeywordName(ATTEMPTS_KEYWORD, "attempts"), 0)
    }

    private fun getInteger(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>,
                           keywordName: String, defaultValue: Int): Int {
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