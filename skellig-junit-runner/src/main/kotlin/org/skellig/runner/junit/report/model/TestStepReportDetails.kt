package org.skellig.runner.junit.report.model

import org.apache.commons.lang3.time.DurationFormatUtils
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.TaskTestStep
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.util.PropertyFormatUtils

/**
 * Represents the details of a test step report.
 *
 * @param T The type of the original test step.
 * @property name The name of the test step.
 * @property parameters The parameters of the test step.
 * @property originalTestStep The original test step object.
 * @property result The result of the test step.
 * @property errorLog The error log of the test step.
 * @property logRecords The log records of the test step.
 * @property duration The duration of the test step execution in milliseconds.
 */
open class TestStepReportDetails<T>(
    val name: String,
    val parameters: Map<String, Any?>?,
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

    fun getParameters(): String {
        return parameters?.map { "${it.key} = ${it.value}" }?.joinToString("\n") ?: ""
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
        private var parameters: Map<String, Any?>? = null

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

        fun withParameters(parameters: Map<String, Any?>?) = apply {
            this.parameters = parameters
        }

        fun build(): TestStepReportDetails<*> {
            return when (originalTestStep) {
                is DefaultTestStep -> DefaultTestStepReportDetails(name!!, parameters, originalTestStep as DefaultTestStep, result, errorLog, logRecords, duration)
                else -> TestStepReportDetails(name!!, parameters, originalTestStep, result, errorLog, logRecords, duration)
            }
        }
    }
}

class DefaultTestStepReportDetails(
    name: String,
    parameters: Map<String, Any?>?,
    originalTestStep: DefaultTestStep?,
    result: Any?,
    errorLog: String?,
    logRecords: List<String>?,
    duration: Long
) : TestStepReportDetails<DefaultTestStep>(name, parameters, originalTestStep, result, errorLog, logRecords, duration) {

    override fun getTestData(): String {
        return PropertyFormatUtils.toString(originalTestStep?.testData ?: "", 0)
    }

    override fun getValidationDetails(): String {
        return originalTestStep?.validationDetails?.toString() ?: ""
    }

}

fun getFormattedDuration(duration: Long): String {
    return if (duration > 60000) {
        DurationFormatUtils.formatDuration(duration, "mm.ss.SSS") + " min."
    } else if (duration >= 1000) {
        DurationFormatUtils.formatDuration(duration, "ss.SSS") + " sec."
    } else DurationFormatUtils.formatDuration(duration, "SSS") + " ms."
}