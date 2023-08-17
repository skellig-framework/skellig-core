package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.util.CachedPattern
import java.util.*

abstract class BaseTestStepFactory<T : TestStep>(
    val keywordsProperties: Properties?,
    val testStepFactoryValueConverter: TestStepFactoryValueConverter
) : TestStepFactory<T> {

    companion object {
        protected const val TEST_STEP_NAME_KEYWORD = "test.step.keyword.name"
    }

    protected open fun <T> convertValue(value: Any?, parameters: Map<String, Any?>): T? {
        return testStepFactoryValueConverter.convertValue(value, parameters)
    }

    protected fun extractParametersFromTestStepName(testStepName: String, rawTestStep: Map<String, Any?>): Map<String, String?>? {
        var parameters: MutableMap<String, String?>? = null
        val matcher = CachedPattern.compile(getName(rawTestStep)).matcher(testStepName)
        if (matcher.find()) {
            parameters = HashMap()
            for (i in 1..matcher.groupCount()) {
                val value = matcher.group(i)
                if (value.isNotEmpty()) {
                    parameters[i.toString()] = value
                }
            }
        }
        return parameters
    }

    protected open fun getName(rawTestStep: Map<String, Any?>): String {
        return rawTestStep[getKeywordName(TEST_STEP_NAME_KEYWORD, "name")].toString()
    }

    protected fun getKeywordName(keywordName: String?, defaultValue: String): String {
        return if (keywordsProperties == null) defaultValue else keywordsProperties.getProperty(keywordName, defaultValue)
    }
}