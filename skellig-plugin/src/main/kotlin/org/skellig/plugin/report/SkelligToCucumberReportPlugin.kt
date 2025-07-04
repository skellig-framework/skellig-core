package org.skellig.plugin.report

import com.fasterxml.jackson.databind.ObjectMapper
import org.skellig.feature.event.*
import org.skellig.plugin.SkelligPlugin
import java.io.File
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.absolute


private const val NANOSECONDS = 1000000
private const val DATETIME_FORMAT = "yyyyMMMddhhmmssSSS"

class SkelligToCucumberReportPlugin(val reportFile: String) : SkelligPlugin {

    constructor(reportDir: String, reportFileNamePrefix: String) :
            this("$reportDir/$reportFileNamePrefix${LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATETIME_FORMAT))}.json")

    private val jsonSerializer = ObjectMapper()

    private val executedFeaturesReportDetails = ConcurrentHashMap<Any, CucumberFeatureReportDetails>()

    override fun init(eventDispatcher: SkelligTestEventDispatcher) {
        eventDispatcher.register(FeatureStartedEvent::class) { e ->

            executedFeaturesReportDetails[e.feature.getId()] =
                CucumberFeatureReportDetails(
                    1,
                    e.feature.getId().toString(),
                    e.feature.getEntityName(),
                    e.feature.filePath,
                    e.feature.getEntityTags()?.map { CucumberTagReportDetails(it) }?.toList() ?: emptyList<CucumberTagReportDetails>()
                )
        }

        eventDispatcher.register(TestScenarioStartedEvent::class) { e ->
            executedFeaturesReportDetails[e.featureId]?.addScenario(
                e.testScenario.getId(),
                CucumberScenarioReportDetails(
                    1,
                    e.testScenario.getId().toString(),
                    e.testScenario.getEntityName(),
                    DateTimeFormatter.ISO_INSTANT.format(e.getEventTime())
                )
            )
        }

        eventDispatcher.register(TestStepStartedEvent::class) { e ->
            executedFeaturesReportDetails[e.parentFeatureId]?.let { executedFeaturesReportDetails ->
                if (e.parentTestScenarioId != null) {
                    executedFeaturesReportDetails.getScenario(e.parentTestScenarioId!!)?.addStep(
                        e.testStep.getId(),
                        CucumberStepReportDetails(
                            1,
                            e.testStep.getId().toString(),
                            e.testStep.name,
                            CucumberStepResultReportDetails(""),
                            CucumberMatchReportDetails(e.testStep.name),
                            e.parameters?.map { p -> CucumberCellsReportDetails(arrayOf(p.key, p.value?.toString() ?: "")) }?.toTypedArray() ?: emptyArray()
                        )
                    )
                }
            }
        }

        eventDispatcher.register(HookFinishedEvent::class) { e ->
            executedFeaturesReportDetails[e.parentFeatureId]?.let { executedFeaturesReportDetails ->
                val hookReportDetails = CucumberHookReportDetails(
                    CucumberStepResultReportDetails(e.result.executionStatus.name.lowercase(), e.result.duration * NANOSECONDS),
                    CucumberMatchReportDetails(e.methodName ?: ""),
                    e.logRecords?.toTypedArray() ?: emptyArray<String>()
                )

                executedFeaturesReportDetails.getScenario(e.parentTestScenarioId ?: -1)?.let {
                    if (e.executionSequenceType == ExecutionSequenceType.BEFORE) {
                        it.addBeforeHook(hookReportDetails)
                    } else if (e.executionSequenceType == ExecutionSequenceType.AFTER) {
                        it.addAfterHook(hookReportDetails)
                    }
                }
            }
        }

        eventDispatcher.register(TestStepFinishedEvent::class) { e ->
            executedFeaturesReportDetails[e.featureId]?.let { executedFeaturesReportDetails ->

                val testStepReportDetails = getTestStepReportDetails(e, executedFeaturesReportDetails)
                testStepReportDetails?.let {
                    it.output = e.logRecords?.toTypedArray() ?: emptyArray()
                }
            }
        }

        eventDispatcher.register(TestStepProcessingFinishedEvent::class) { e ->
            executedFeaturesReportDetails[e.featureId]?.let { executedFeaturesReportDetails ->
                executedFeaturesReportDetails.getScenario(e.testScenarioId!!)?.getStep(e.testStepId)
                    ?.updateResult(e.result)
            }
        }

        eventDispatcher.register(TestRunFinishedEvent::class) { e ->
            val reportFilePath = Paths.get(reportFile).absolute().toString()
            File(reportFilePath.substringBeforeLast("\\").substringBeforeLast("/")).mkdirs()
            File(reportFilePath).createNewFile()
            jsonSerializer.writeValue(File(reportFilePath), executedFeaturesReportDetails.values.toList())
        }
    }

    private fun getTestStepReportDetails(
        e: BaseTestStepExecutionEvent,
        executedFeaturesReportDetails: CucumberFeatureReportDetails
    ): CucumberStepReportDetails? {
        return executedFeaturesReportDetails.getScenario(e.testScenarioId!!)?.getStep(e.testStepId)
    }

    override fun getName(): String = "SkelligToCucumberReport"

    abstract inner class CucumberElementReportDetails(
        val line: Int,
        val id: String,
        val name: String,
    ) {

        abstract fun getKeyword(): String

        abstract fun getType(): String
    }

    inner class CucumberFeatureReportDetails(
        line: Int,
        id: String,
        name: String,
        val uri: String,
        val tags: List<CucumberTagReportDetails> = emptyList(),
        description: String = ""
    ) : CucumberElementReportDetails(line, id, name) {

        private val elements = mutableMapOf<Int, CucumberElementReportDetails>()

        override fun getKeyword(): String = "Feature"

        override fun getType(): String = "feature"

        fun getElements(): Collection<CucumberElementReportDetails> = elements.values

        fun addScenario(scenarioId: Int, cucumberScenarioReportDetails: CucumberScenarioReportDetails) {
            elements[scenarioId] = cucumberScenarioReportDetails
        }

        fun getScenario(scenarioId: Int): CucumberScenarioReportDetails? {
            return elements[scenarioId] as CucumberScenarioReportDetails?
        }
    }

    inner class CucumberScenarioReportDetails(
        line: Int,
        id: String,
        name: String,
        val start_timestamp: Any,
        val before: MutableList<CucumberHookReportDetails> = mutableListOf(),
        val after: MutableList<CucumberHookReportDetails> = mutableListOf(),
        val tags: List<CucumberTagReportDetails> = emptyList(),
        description: String = "",
    ) : CucumberElementReportDetails(line, id, name) {

        private val steps = mutableMapOf<Int, CucumberStepReportDetails>()

        override fun getKeyword(): String = "Scenario"

        override fun getType(): String = "scenario"

        fun addStep(testStepId: Int, cucumberStepReportDetails: CucumberStepReportDetails) {
            steps.put(testStepId, cucumberStepReportDetails)
        }

        fun addBeforeHook(cucumberHookReportDetails: CucumberHookReportDetails) {
            before.add(cucumberHookReportDetails)
        }

        fun addAfterHook(cucumberHookReportDetails: CucumberHookReportDetails) {
            after.add(cucumberHookReportDetails)
        }

        fun getStep(testStepId: Int): CucumberStepReportDetails? = steps[testStepId]

        fun getSteps(): Collection<CucumberStepReportDetails> = steps.values
    }

    inner class CucumberStepReportDetails(
        line: Int,
        id: String,
        name: String,
        val result: CucumberStepResultReportDetails,
        val match: CucumberMatchReportDetails,
        var rows: Array<CucumberCellsReportDetails>? = null,
        var output: Array<String>? = null,
    ) : CucumberElementReportDetails(line, id, name) {

        override fun getKeyword(): String = ""

        override fun getType(): String = "step"

        fun updateResult(newResult: Result) {
            result.status = newResult.executionStatus.name.lowercase()
            result.duration = newResult.duration * NANOSECONDS
            result.errorMessage = newResult.errorLog
        }
    }

    inner class CucumberHookReportDetails(
        val result: CucumberStepResultReportDetails,
        val match: CucumberMatchReportDetails,
        var output: Array<String>? = emptyArray<String>()
    )

    inner class CucumberMatchReportDetails(
        val location: String,
        val arguments: Array<Any> = emptyArray<Any>()
    )

    inner class CucumberStepResultReportDetails(
        var status: String,
        var duration: Long? = 0,
        var errorMessage: String? = null
    )

    inner class CucumberTagReportDetails(val name: String) {
    }

    inner class CucumberCellsReportDetails(val cells: Array<String>)
}