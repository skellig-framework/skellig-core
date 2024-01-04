package org.skellig.runner.junit.report.model

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.GroupedTestStep
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.util.PropertyFormatUtils

open class TestStepReportDetails<T>(
    val name: String,
    val originalTestStep: T?,
    val result: Any?,
    val errorLog: String?,
    val logRecords: List<String>?
) {

    fun isPassed(): Boolean {
        return errorLog == null || errorLog == ""
    }

    fun isIgnored(): Boolean {
        return originalTestStep == null && result == null
    }

    open fun getTestData(): String = ""

    open fun getValidationDetails(): String = ""

    fun getProperties(): String {
        if (originalTestStep != null && originalTestStep.javaClass != TestStep::class.java && originalTestStep is TestStep) {
            return originalTestStep.toString()
        }
        return ""
    }

    fun getResult(): String {
        return PropertyFormatUtils.toString(result, 0)
    }

    class Builder {
        private var name: String? = null
        private var originalTestStep: Any? = null
        private var result: Any? = null
        private var errorLog: String? = null
        private var logRecords: List<String>? = null

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

        fun withLogRecords(logRecords: List<String>?) = apply {
            this.logRecords = logRecords
        }

        fun build(): TestStepReportDetails<*> {
            return when (originalTestStep) {
                is DefaultTestStep -> DefaultTestStepReportDetails(name!!, originalTestStep as DefaultTestStep, result, errorLog, logRecords)
                is GroupedTestStep -> GroupedTestStepReportDetails(name!!, originalTestStep as GroupedTestStep, result, errorLog, logRecords)
                else -> TestStepReportDetails(name!!, originalTestStep, result, errorLog, logRecords)
            }
        }
    }
}

class DefaultTestStepReportDetails(
    name: String,
    originalTestStep: DefaultTestStep?,
    result: Any?,
    errorLog: String?,
    logRecords: List<String>?
) : TestStepReportDetails<DefaultTestStep>(name, originalTestStep, result, errorLog, logRecords) {

    override fun getTestData(): String {
        return PropertyFormatUtils.toString(originalTestStep?.testData?.toString() ?: "", 0)
    }

    override fun getValidationDetails(): String {
        return originalTestStep?.validationDetails?.toString() ?: ""
    }

}

class GroupedTestStepReportDetails(
    name: String,
    originalTestStep: GroupedTestStep?,
    result: Any?,
    errorLog: String?,
    logRecords: List<String>?
) : TestStepReportDetails<GroupedTestStep>(name, originalTestStep, result, errorLog, logRecords)