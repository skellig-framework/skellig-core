package org.skellig.runner.plugin

import org.apache.commons.lang3.time.DurationFormatUtils
import org.skellig.feature.event.*
import org.skellig.runner.junit.report.ReportGenerator
import org.skellig.runner.junit.report.SkelligReportGenerator
import org.skellig.runner.junit.report.model.FeatureReportDetails
import org.skellig.runner.junit.report.model.HookReportDetails
import org.skellig.runner.junit.report.model.TestScenarioReportDetails
import org.skellig.teststep.processing.util.PropertyFormatUtils
import java.util.concurrent.ConcurrentHashMap

class SkelligReportPlugin(reportDir: String) : SkelligPlugin {

    private val reportGenerator: ReportGenerator = SkelligReportGenerator(reportDir)
    private val executedFeaturesReportDetails = ConcurrentHashMap<Any, FeatureReportDetails>()

    override fun init(eventDispatcher: SkelligTestEventDispatcher) {
        eventDispatcher.register(FeatureStartedEvent::class) { e ->
            executedFeaturesReportDetails[e.feature.getId()] =
                FeatureReportDetails(
                    name = e.feature.getEntityName(),
                    tags = e.feature.getEntityTags()
                )
        }

        eventDispatcher.register(TestScenarioStartedEvent::class) { e ->
            executedFeaturesReportDetails[e.featureId]?.let {
                it.testScenarioReportDetails[e.testScenario.getId()] =
                    TestScenarioReportDetails(
                        name = e.testScenario.getEntityName(),
                        tags = e.testScenario.getEntityTags()
                    )
            }
        }

        eventDispatcher.register(TestStepStartedEvent::class) { e ->
            executedFeaturesReportDetails[e.parentFeatureId]?.let { executedFeaturesReportDetails ->
                val testStepReportDetails = if (e.executionSequenceType == ExecutionSequenceType.BEFORE) {
                    if (e.parentTestScenarioId == null) executedFeaturesReportDetails.beforeReportDetails
                    else executedFeaturesReportDetails.testScenarioReportDetails[e.parentTestScenarioId!!]?.beforeReportDetails
                } else if (e.executionSequenceType == ExecutionSequenceType.AFTER) {
                    if (e.parentTestScenarioId == null) executedFeaturesReportDetails.afterReportDetails
                    else executedFeaturesReportDetails.testScenarioReportDetails[e.parentTestScenarioId!!]?.afterReportDetails
                } else {
                    executedFeaturesReportDetails.testScenarioReportDetails[e.parentTestScenarioId!!]?.testStepReportDetails
                }

                testStepReportDetails?.put(e.testStep.getId(), TestStepReportDetails(e.testStep.name, e.parameters))
            }
        }

        eventDispatcher.register(HookFinishedEvent::class) { e ->
            executedFeaturesReportDetails[e.parentFeatureId]?.let { executedFeaturesReportDetails ->
                val hookReportDetails = HookReportDetails(e.methodName, e.result, e.logRecords)

                if (e.executionSequenceType == ExecutionSequenceType.BEFORE) {
                    if (e.parentTestScenarioId == null) executedFeaturesReportDetails.beforeHooksReportDetails?.add(hookReportDetails)
                    else executedFeaturesReportDetails.testScenarioReportDetails[e.parentTestScenarioId ?: ""]?.beforeHooksReportDetails?.add(hookReportDetails)
                } else if (e.executionSequenceType == ExecutionSequenceType.AFTER) {
                    if (e.parentTestScenarioId == null) executedFeaturesReportDetails.afterHooksReportDetails.add(hookReportDetails)
                    else executedFeaturesReportDetails.testScenarioReportDetails[e.parentTestScenarioId ?: ""]?.afterHooksReportDetails?.add(hookReportDetails)
                }
            }
        }

        eventDispatcher.register(TestStepFinishedEvent::class) { e ->
            executedFeaturesReportDetails[e.featureId]?.let { executedFeaturesReportDetails ->

                val testStepReportDetails = getTestStepReportDetails(e, executedFeaturesReportDetails)
                testStepReportDetails?.let {
                    it.logRecords = e.logRecords
                }
            }
        }

        eventDispatcher.register(TestStepProcessingFinishedEvent::class) { e ->
            executedFeaturesReportDetails[e.featureId]?.let { executedFeaturesReportDetails ->
                val testStepReportDetails = getTestStepReportDetails(e, executedFeaturesReportDetails)
                testStepReportDetails?.let {
                    //TODO: assign test step info and extract data from map in the FTL
                    it.testData = e.testStepInfo["Test Data"]
                    it.properties = e.testStepInfo["Properties"]
                    it.validationDetails = e.testStepInfo["Validation Details"]
                    it.result = e.result.result
                    it.errorLog = e.result.errorLog
                    it.duration = e.result.duration
                    it.executionStatus = e.result.executionStatus
                }
            }
        }

        eventDispatcher.register(TestRunFinishedEvent::class) { e ->
            reportGenerator.generate(executedFeaturesReportDetails.values.toList())
        }
    }

    private fun getTestStepReportDetails(
        e: BaseTestStepExecutionEvent,
        executedFeaturesReportDetails: FeatureReportDetails
    ): TestStepReportDetails? {
        val testStepReportDetails = if (e.executionSequenceType == ExecutionSequenceType.BEFORE) {
            if (e.testScenarioId == null) executedFeaturesReportDetails.beforeReportDetails[e.testStepId]
            else executedFeaturesReportDetails.testScenarioReportDetails[e.testScenarioId!!]?.beforeReportDetails?.get(e.testStepId)
        } else if (e.executionSequenceType == ExecutionSequenceType.AFTER) {
            if (e.testScenarioId == null) executedFeaturesReportDetails.afterReportDetails[e.testStepId]
            else executedFeaturesReportDetails.testScenarioReportDetails[e.testScenarioId!!]?.afterReportDetails?.get(e.testStepId)
        } else {
            executedFeaturesReportDetails.testScenarioReportDetails[e.testScenarioId!!]?.testStepReportDetails?.get(e.testStepId)
        }
        return testStepReportDetails
    }

    override fun getName(): String = "skelligReport"
}

open class TestStepReportDetails(
    val name: String,
    val parameters: Map<String, Any?>?,
    var testData: Any?,
    var properties: String?,
    var validationDetails: Any?,
    var result: Any?,
    var executionStatus: TestExecutionStatus,
    var errorLog: String?,
    var logRecords: List<String>?,
    var duration: Long
) {

    constructor(name: String, parameters: Map<String, Any?>?) : this(name, parameters, null, null, null, null, TestExecutionStatus.RUNNING, null, null, 0)

    fun isPassed(): Boolean {
        return errorLog == null || errorLog == ""
    }

    fun isIgnored(): Boolean {
        return testData == null && result == null
    }

    fun getTestData(): String {
        return PropertyFormatUtils.toString(testData ?: "", 0)
    }

    fun getValidationDetails(): String {
        return validationDetails?.toString() ?: ""
    }

    fun getDurationFormatted(): String {
        return getFormattedDuration(duration)
    }

    fun getParameters(): String {
        return parameters?.map { "${it.key} = ${it.value}" }?.joinToString("\n") ?: ""
    }

    fun getResult(): String {
        return PropertyFormatUtils.toString(result, 0)
    }
}

fun getFormattedDuration(duration: Long): String {
    return if (duration > 60000) {
        DurationFormatUtils.formatDuration(duration, "mm.ss.SSS") + " min."
    } else if (duration >= 1000) {
        DurationFormatUtils.formatDuration(duration, "ss.SSS") + " sec."
    } else DurationFormatUtils.formatDuration(duration, "SSS") + " ms."
}