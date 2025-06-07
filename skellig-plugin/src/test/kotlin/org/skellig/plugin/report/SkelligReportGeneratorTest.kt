package org.skellig.plugin.report

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.skellig.feature.event.TestExecutionStatus
import org.skellig.plugin.report.model.FeatureReportDetails
import org.skellig.plugin.report.model.HookReportDetails
import org.skellig.plugin.report.model.TestScenarioReportDetails
import org.skellig.plugin.report.model.TestStepReportDetails
import org.skellig.feature.event.Result
import java.io.File
import java.nio.file.Path

class SkelligReportGeneratorTest {

    private lateinit var reportGenerator: SkelligReportGenerator
    private lateinit var reportDirectory: String

    @BeforeEach
    fun setUp() {
        reportDirectory = "report/tmp"
        reportGenerator = SkelligReportGenerator(reportDirectory)
    }

    @AfterEach
    fun tearDown() {
        // Clean up any created files
        File("target/$reportDirectory").listFiles()?.forEach { file ->
            if (file.isDirectory) {
                file.deleteRecursively()
            } else {
                file.delete()
            }
        }
    }

    @Test
    fun `test generate creates HTML report file`() {
        // Create a FeatureReportDetails with all properties set
        val featureReportDetails = createFeatureReportDetails()

        // Generate the report
        reportGenerator.generate(listOf(featureReportDetails))

        // Check that the HTML report file is created
        val reportFile = File("target/$reportDirectory/skellig-report/index.html")
        assertTrue(reportFile.exists(), "Report file was not created")
    }

    private fun createFeatureReportDetails(): FeatureReportDetails {
        // Create a hook report detail
        val hookReportDetail = HookReportDetails(
            methodName = "testHook",
            result = Result(100L, null, "hookResult"),
            logRecords = listOf("Hook log record")
        )

        // Create a test step report detail
        val testStepReportDetail = TestStepReportDetails(
            name = "Test Step",
            parameters = mapOf("param1" to "value1"),
            testStepInfo = mapOf("Properties" to "some properties"),
            result = "Test step result",
            executionStatus = TestExecutionStatus.PASSED,
            errorLog = null,
            logRecords = listOf("Test step log"),
            duration = 200L
        )

        // Create a test scenario report detail
        val testScenarioReportDetail = TestScenarioReportDetails(
            name = "Test Scenario",
            tags = setOf("tag1", "tag2"),
            beforeHooksReportDetails = mutableListOf(hookReportDetail),
            afterHooksReportDetails = mutableListOf(hookReportDetail),
            beforeReportDetails = mutableMapOf("beforeKey" to testStepReportDetail),
            afterReportDetails = mutableMapOf("afterKey" to testStepReportDetail),
            testStepReportDetails = mutableMapOf("stepKey" to testStepReportDetail)
        )

        // Create the feature report details
        return FeatureReportDetails(
            name = "Test Feature",
            tags = setOf("featureTag1", "featureTag2"),
            beforeHooksReportDetails = mutableListOf(hookReportDetail),
            afterHooksReportDetails = mutableListOf(hookReportDetail),
            beforeReportDetails = mutableMapOf("featureBeforeKey" to testStepReportDetail),
            afterReportDetails = mutableMapOf("featureAfterKey" to testStepReportDetail),
            testScenarioReportDetails = mutableMapOf("scenarioKey" to testScenarioReportDetail)
        )
    }
}
