package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.converter.TestDataConverter
import org.skellig.teststep.processing.converter.TestStepValueConverter
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.util.CachedPattern
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.HashMap

abstract class BaseTestStepFactory(
        val keywordsProperties: Properties?,
        val testStepValueConverter: TestStepValueConverter?,
        val testDataConverter: TestDataConverter?) : TestStepFactory {

    companion object {
        private val COMMA_SPLIT_PATTERN = Pattern.compile(",")

        private const val TEST_STEP_NAME_KEYWORD = "test.step.keyword.name"
        private const val VARIABLES_KEYWORD = "test.step.keyword.variables"
        private const val EXECUTION_KEYWORD = "test.step.keyword.execution"
        private const val TIMEOUT_KEYWORD = "test.step.keyword.timeout"
        private const val DELAY_KEYWORD = "test.step.keyword.delay"
        private const val DEFAULT_DELAY = 5
        private const val DEFAULT_TIMEOUT = 30

        private var testDataKeywords: Set<String>? = null
    }

    private var testStepFactoryValueConverter = TestStepFactoryValueConverter(testStepValueConverter)
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

    override fun create(testStepName: String, rawTestStep: Map<String, Any?>, parameters: Map<String, String?>): TestStep {
        val additionalParameters: MutableMap<String, Any?> = HashMap(parameters)
        val parametersFromTestName = extractParametersFromTestStepName(testStepName, rawTestStep)
        if (parametersFromTestName != null) {
            additionalParameters.putAll(parametersFromTestName)
        }

        val variables = extractVariables(rawTestStep, additionalParameters)
        if (variables != null) {
            additionalParameters.putAll(variables)
        }

        return createTestStepBuilder(rawTestStep, additionalParameters)
                .withId(getId(rawTestStep, additionalParameters))
                .withName(testStepName)
                .withTestData(extractTestData(rawTestStep, additionalParameters))
                .withValidationDetails(validationDetailsFactory.create(rawTestStep, additionalParameters))
                .withVariables(variables)
                .withExecution(getExecutionType(rawTestStep))
                .withTimeout(getTimeout(rawTestStep, additionalParameters))
                .withDelay(getTimeout(rawTestStep, additionalParameters))
                .build()
    }

    protected abstract fun createTestStepBuilder(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): TestStep.Builder

    protected open fun getStringArrayDataFromRawTestStep(propertyName: String?, rawTestStep: Map<String, Any?>,
                                                         parameters: Map<String, Any?>): Collection<String>? {
        val rawArrayData = rawTestStep[propertyName]
        if (rawArrayData != null) {
            when {
                rawArrayData is String -> {
                    return COMMA_SPLIT_PATTERN.split(convertValue(rawArrayData, parameters)).toList()
                }
                rawArrayData is Collection<*> -> return rawArrayData as Collection<String>
                rawArrayData.javaClass.isArray -> return (rawArrayData as Array<String>).toList()
            }
        }
        return null
    }

    protected open fun getTestDataKeywords(): Set<String>? {
        return testDataKeywords
    }

    private fun extractVariables(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): Map<String, Any?>? {
        val rawVariables = rawTestStep[getKeywordName(VARIABLES_KEYWORD, "variables")]
        val convertedVariables = convertHierarchicalData(rawVariables, parameters)
        return if (convertedVariables is Map<*, *>) convertedVariables as Map<String, Any?> else null
    }

    private fun extractTestData(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): Any? {
        return getTestDataKeywords()!!
                .filter { rawTestStep.containsKey(it) }
                .map { keyword: String -> testDataConverter!!.convert(convertHierarchicalData(rawTestStep[keyword], parameters)) }
                .firstOrNull()
    }

    private fun convertHierarchicalData(data: Any?, parameters: Map<String, Any?>): Any? {
        return when (data) {
            is Map<*, *> -> data.entries.map { it.key to convertHierarchicalData(it.value, parameters) }.toMap()
            is List<*> -> data.map { convertHierarchicalData(it, parameters) }.toList()
            else -> testStepFactoryValueConverter.convertValue<Any>(data, parameters)
        }
    }

    private fun extractParametersFromTestStepName(testStepName: String, rawTestStep: Map<String, Any?>): Map<String, String>? {
        var parameters: MutableMap<String, String>? = null
        val matcher = CachedPattern.compile(getName(rawTestStep)).matcher(testStepName)
        if (matcher.find()) {
            parameters = HashMap()
            for (i in 1..matcher.groupCount()) {
                parameters[i.toString()] = matcher.group(i)
            }
        }
        return parameters
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
        return getInteger(rawTestStep, parameters, getKeywordName(DELAY_KEYWORD, "delay"), DEFAULT_DELAY)
    }

    protected open fun getName(rawTestStep: Map<String, Any?>): String {
        return rawTestStep[getKeywordName(TEST_STEP_NAME_KEYWORD, "name")].toString()
    }

    protected fun getKeywordName(keywordName: String?, defaultValue: String): String {
        return if (keywordsProperties == null) defaultValue else keywordsProperties.getProperty(keywordName, defaultValue)
    }

    protected open fun <T> convertValue(value: Any?, parameters: Map<String, Any?>): T? {
        return testStepFactoryValueConverter.convertValue(value, parameters)
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