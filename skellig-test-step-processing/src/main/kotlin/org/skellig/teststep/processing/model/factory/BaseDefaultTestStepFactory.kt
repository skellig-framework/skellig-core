package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.converter.TestStepValueConverter
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.TestStepExecutionType
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.HashMap

abstract class BaseDefaultTestStepFactory<T : DefaultTestStep>(
        keywordsProperties: Properties?,
        testStepValueConverter: TestStepValueConverter?)
    : BaseTestStepFactory<T>(keywordsProperties, testStepValueConverter) {

    companion object {
        private val COMMA_SPLIT_PATTERN = Pattern.compile(",")

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
        val additionalParameters: MutableMap<String, Any?> = HashMap(parameters)
        val parametersFromTestName = extractParametersFromTestStepName(testStepName, rawTestStep)
        parametersFromTestName?.let {
            additionalParameters.putAll(parametersFromTestName)
        }

        val variables = extractVariables(rawTestStep, additionalParameters)
        variables?.let {
            additionalParameters.putAll(variables)
        }

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

    private fun extractVariables(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): Map<String, Any?>? {
        val rawVariables = rawTestStep[getKeywordName(VARIABLES_KEYWORD, "variables")]
        val convertedVariables = convertValue<Any>(rawVariables, parameters)
        return if (convertedVariables is Map<*, *>) convertedVariables as Map<String, Any?> else null
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