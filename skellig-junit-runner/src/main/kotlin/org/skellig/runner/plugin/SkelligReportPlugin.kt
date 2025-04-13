package org.skellig.runner.plugin

import org.skellig.feature.event.*
import org.skellig.runner.junit.report.ReportGenerator
import org.skellig.runner.junit.report.SkelligReportGenerator
import org.skellig.runner.junit.report.model.FeatureReportDetails
import org.skellig.runner.junit.report.model.HookReportDetails
import org.skellig.runner.junit.report.model.TestScenarioReportDetails
import org.skellig.runner.junit.report.model.TestStepReportDetails
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
                    it.testStepInfo = e.testStepInfo
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