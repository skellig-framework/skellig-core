package org.skellig.runner.junit.report.model

import org.apache.commons.lang3.time.DurationFormatUtils
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.GroupedTestStep
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.util.PropertyFormatUtils

open class TestStepReportDetails<T>(
    val name: String,
    val originalTestStep: T?,
    val result: Any?,
    val errorLog: String?,
    val logRecords: List<String>?,
    var duration: Long
) {

    fun isPassed(): Boolean {
        return errorLog == null || errorLog == ""
    }

    fun isIgnored(): Boolean {
        return originalTestStep == null && result == null
    }

    open fun getTestData(): String = ""

    open fun getValidationDetails(): String = ""

    fun getDurationFormatted(): String {
        return getFormattedDuration(duration)
    }

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
        private var duration: Long = 0

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

        fun withDuration(duration: Long) = apply {
            this.duration = duration
        }

        fun build(): TestStepReportDetails<*> {
            return when (originalTestStep) {
                is DefaultTestStep -> DefaultTestStepReportDetails(name!!, originalTestStep as DefaultTestStep, result, errorLog, logRecords, duration)
                is GroupedTestStep -> GroupedTestStepReportDetails(name!!, originalTestStep as GroupedTestStep, result, errorLog, logRecords, duration)
                else -> TestStepReportDetails(name!!, originalTestStep, result, errorLog, logRecords, duration)
            }
        }
    }
}

class DefaultTestStepReportDetails(
    name: String,
    originalTestStep: DefaultTestStep?,
    result: Any?,
    errorLog: String?,
    logRecords: List<String>?,
    duration: Long
) : TestStepReportDetails<DefaultTestStep>(name, originalTestStep, result, errorLog, logRecords, duration) {

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
    logRecords: List<String>?,
    duration: Long
) : TestStepReportDetails<GroupedTestStep>(name, originalTestStep, result, errorLog, logRecords, duration)

fun getFormattedDuration(duration: Long): String {
    return if (duration > 60000) {
        DurationFormatUtils.formatDuration(duration, "mm.ss.SSS") + " min."
    } else if (duration >= 1000) {
        DurationFormatUtils.formatDuration(duration, "ss.SSS") + " sec."
    } else DurationFormatUtils.formatDuration(duration, "SSS") + " ms."
}