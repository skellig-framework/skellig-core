package org.skellig.runner.junit.report.model

import org.apache.commons.lang3.StringUtils
import org.skellig.teststep.processing.model.ExpectedResult
import org.skellig.teststep.processing.model.TestStep
import java.beans.Introspector
import java.beans.PropertyDescriptor
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

class TestStepReportDetails(val name: String,
                            val originalTestStep: Any?,
                            val result: Any?,
                            val errorLog: String?) {

    fun isPassed(): Boolean {
        return errorLog == null || errorLog == ""
    }

    fun isIgnored(): Boolean {
        return originalTestStep == null && result == null
    }

    fun getTestData(): String {
        var result = ""
        if (originalTestStep is TestStep) {
            val testData = originalTestStep.testData
            result = testData?.toString() ?: ""
        }
        return result
    }

    fun getValidationDetails(): String {
        val stringBuilder = StringBuilder()
        if (originalTestStep is TestStep) {
            originalTestStep.validationDetails?.let { constructValidationDetails(it.expectedResult, stringBuilder) }
        }
        return stringBuilder.toString()
    }

    fun getProperties(): String {
        if (originalTestStep != null && originalTestStep.javaClass != TestStep::class.java && originalTestStep is TestStep) {

            val properties = getPropertyGettersOfTestStep(originalTestStep.javaClass)
            return properties.entries
                    .filter { it.value != null }
                    .map { getPropertyWithValue(it) }
                    .joinToString("\n")
        }
        return ""
    }

    fun getResult(): String {
        return result?.toString() ?: ""
    }

    private fun constructValidationDetails(expectedResult: ExpectedResult, stringBuilder: StringBuilder) {
        if (StringUtils.isNotEmpty(expectedResult.property)) {
            stringBuilder.append(expectedResult.property).append(": ")
        }
        if (expectedResult.expectedResult is List<*>) {
            stringBuilder.append(getValidationType(expectedResult)).append("[\n")
            expectedResult.get<List<ExpectedResult>>()?.forEach { item -> constructValidationDetails(item, stringBuilder) }
            stringBuilder.append("]\n")
        } else if (expectedResult.expectedResult != null) {
            stringBuilder.append(expectedResult.expectedResult.toString()).append("\n")
        }
    }

    private fun getValidationType(expectedResult: ExpectedResult): String? {
        return expectedResult.matchingType?.name?.toLowerCase()
    }

    private fun getPropertyWithValue(propertyGetterPair: Map.Entry<String, Method?>): String? {
        return try {
            propertyGetterPair.value?.invoke(originalTestStep)
                    ?.let { propertyGetterPair.key + ": " + it }
                    ?: ""
        } catch (e: IllegalAccessException) {
            e.message
        } catch (e: InvocationTargetException) {
            e.message
        }
    }

    private fun getPropertyGettersOfTestStep(beanClass: Class<*>): Map<String, Method?> {
        return beanClass.declaredFields
                .map {
                    it.name to Introspector.getBeanInfo(beanClass).propertyDescriptors
                            .filter { pd: PropertyDescriptor -> pd.readMethod != null && it.name == pd.name }
                            .map { obj: PropertyDescriptor -> obj.readMethod }
                            .firstOrNull()
                }
                .toMap()

    }

    class Builder {
        private var name: String? = null
        private var originalTestStep: Any? = null
        private var result: Any? = null
        private var errorLog: String? = null

        fun withName(name: String?) = apply {
            this.name = name
        }

        fun withOriginalTestStep(originalTestStep: Any?) = apply {
            this.originalTestStep = originalTestStep
        }

        fun withResult(result: Any?) = apply {
            this.result = result
        }

        fun withErrorLog(errorLog: String?) = apply {
            this.errorLog = errorLog
        }

        fun build(): TestStepReportDetails {
            return TestStepReportDetails(name!!, originalTestStep, result, errorLog)
        }
    }
}