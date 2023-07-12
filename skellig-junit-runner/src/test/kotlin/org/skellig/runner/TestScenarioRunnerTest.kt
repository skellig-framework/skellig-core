package org.skellig.runner

import com.nhaarman.mockitokotlin2.*
import com.typesafe.config.Config
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.runner.Description
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier
import org.skellig.feature.TestScenario
import org.skellig.feature.TestStep
import org.skellig.runner.TestScenarioRunner.Companion.REPORT_LOG_ENABLED
import org.skellig.runner.junit.report.attachment.log.LogAttachment
import org.skellig.runner.junit.report.attachment.log.fromfile.LogRecordsFromFileAttachment
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.runner.TestStepRunner

private const val TEST_STEP_1 = "Test 1"
private const val TEST_STEP_2 = "Test 2"
private const val SCENARIO_NAME = "Scenario 1"

class TestScenarioRunnerTest {

    var testStepRunner: TestStepRunner = mock()
    var config: Config = mock()

    @Test
    fun testRunScenario() {
        val testStep = mock<org.skellig.teststep.processing.model.TestStep>()
        val testStepRunResult = TestStepProcessor.TestStepRunResult(testStep)
        whenever(testStepRunner.run(eq(TEST_STEP_1), any())).thenReturn(testStepRunResult)
        testStepRunResult.notify("response", null)

        val testScenarioRunner = TestScenarioRunner.create(createTestScenario(), testStepRunner, config)

        val notifier = mock<RunNotifier>()
        testScenarioRunner.run(notifier)

        val reportDetails = testScenarioRunner.getTestScenarioReportDetails()

        assertAll(
            { assertEquals(TEST_STEP_1, reportDetails.testStepReportDetails!![0].name) },
            { assertEquals(testStep, reportDetails.testStepReportDetails!![0].originalTestStep) },
            { assertEquals("response", reportDetails.testStepReportDetails!![0].result) },
            { assertNull(reportDetails.testStepReportDetails!![0].errorLog) },
            { verify(notifier).fireTestFinished(argThat { o ->
                (o as Description).displayName == "$TEST_STEP_1($SCENARIO_NAME)"
            }) }
        )
    }

    @Nested
    inner class WithFailedSteps {
        @Test
        fun testRunScenarioWhenFailsInternally() {
            val testStepRunResult = TestStepProcessor.TestStepRunResult(mock())
            whenever(testStepRunner.run(eq(TEST_STEP_1), any())).thenReturn(testStepRunResult)
            testStepRunResult.notify(null, RuntimeException("oops"))

            val testScenarioRunner = TestScenarioRunner.create(createTestScenario(), testStepRunner, config)

            val notifier = mock<RunNotifier>()
            testScenarioRunner.run(notifier)

            val reportDetails = testScenarioRunner.getTestScenarioReportDetails()

            assertAll(
                { assertTrue(reportDetails.testStepReportDetails!![0].errorLog!!.contains("RuntimeException: oops"), "Invalid error stack trace") },
                { assertEquals(TEST_STEP_1, reportDetails.testStepReportDetails!![0].name) },
                // verify that fireTestFinished is called anyway
                { verify(notifier).fireTestFinished(argThat { o ->
                    (o as Description).displayName == "$TEST_STEP_1($SCENARIO_NAME)"
                }) }
            )
        }

        @Test
        fun testRunScenarioWhenFailsOnRun() {
            whenever(testStepRunner.run(eq(TEST_STEP_1), any())).thenThrow(RuntimeException::class.java)

            val testScenarioRunner = TestScenarioRunner.create(createTestScenario(), testStepRunner, config)

            testScenarioRunner.run(mock())

            val reportDetails = testScenarioRunner.getTestScenarioReportDetails()

            assertTrue(reportDetails.testStepReportDetails!![0].errorLog!!.contains("java.lang.RuntimeException"), "Invalid error stack trace")
        }

        @Test
        fun testRunScenarioWithAsyncTestFailsInternally() {
            val testStep = DefaultTestStep(name = TEST_STEP_1, timeout = 2000)
            val testStepRunResult = BaseTestStepProcessor.DefaultTestStepRunResult(testStep)
            whenever(testStepRunner.run(eq(TEST_STEP_1), any())).thenReturn(testStepRunResult)

            val testScenarioRunner = TestScenarioRunner.create(createTestScenario(), testStepRunner, config)

            Thread {
                Thread.sleep(200) // simulate async test step and send error with delay
                testStepRunResult.notify(null, RuntimeException("oops"))
            }.start()
            val notifier = mock<RunNotifier>()

            testScenarioRunner.run(notifier)

            verify(notifier).fireTestFailure(argThat { o ->
                (o as Failure).description.displayName == SCENARIO_NAME &&
                        o.message == "Failed to process test step '$TEST_STEP_1'"
            })

        }

        @Test
        fun testRunScenarioWhenFirstFailsAndSecondSkipped() {
            val notifier = mock<RunNotifier>()
            whenever(testStepRunner.run(eq(TEST_STEP_1), any())).thenThrow(RuntimeException::class.java)

            val testScenario = createTestScenarioWith2Steps()
            val testScenarioRunner = TestScenarioRunner.create(testScenario, testStepRunner, config)

            testScenarioRunner.run(notifier)

            verify(notifier).fireTestIgnored(argThat { o ->
                (o as Description).displayName == "$TEST_STEP_2($SCENARIO_NAME)"
            })
        }
    }

    @Nested
    inner class WithAttachment {
        @Test
        fun testRunScenarioWithLogAttachment() {
            whenever(testStepRunner.run(eq(TEST_STEP_1), any())).thenReturn(TestStepProcessor.TestStepRunResult(mock()))
            whenever(config.hasPath(REPORT_LOG_ENABLED)).thenReturn(true)
            whenever(config.getBoolean(REPORT_LOG_ENABLED)).thenReturn(true)


            val testScenarioRunner = TestScenarioRunner.create(createTestScenario(), testStepRunner, config)

            testScenarioRunner.run(mock())


            val reportDetails = testScenarioRunner.getTestScenarioReportDetails()

            assertAll(
                { assertEquals(1, reportDetails.testStepReportDetails!![0].attachments.size) },
                { assertTrue(reportDetails.testStepReportDetails!![0].attachments.any { it is LogAttachment }, "Report must have log attachment") }
            )
        }

        @Test
        fun testRunScenarioWithAttachment() {
            whenever(testStepRunner.run(eq(TEST_STEP_1), any())).thenThrow(RuntimeException::class.java)
            whenever(config.hasPath("report.attachments.recordsFromLogFile.paths")).thenReturn(true)
            whenever(config.getAnyRefList("report.attachments.recordsFromLogFile.paths"))
                .thenReturn(
                    listOf(
                        mapOf(
                            Pair("host", "localhost"),
                            // doesn't matter that file doesn't exist as we check only type of attachment added to the report
                            Pair("path", "any/path/non-exist-file.txt")
                        )
                    )
                )


            val testScenarioRunner = TestScenarioRunner.create(createTestScenario(), testStepRunner, config)

            testScenarioRunner.run(mock())


            val reportDetails = testScenarioRunner.getTestScenarioReportDetails()

            assertAll(
                { assertEquals(1, reportDetails.testStepReportDetails!![0].attachments.size) },
                {
                    assertTrue(
                        reportDetails.testStepReportDetails!![0].attachments.any { it is LogRecordsFromFileAttachment },
                        "Report must have log record from file attachment"
                    )
                }
            )
        }
    }

    private fun createTestScenario(): TestScenario {
        return TestScenario.Builder()
            .withName(SCENARIO_NAME)
            .withStep(TestStep.Builder().withName(TEST_STEP_1))
            .build()[0]
    }

    private fun createTestScenarioWith2Steps(): TestScenario {
        return TestScenario.Builder()
            .withName(SCENARIO_NAME)
            .withStep(TestStep.Builder().withName(TEST_STEP_1))
            .withStep(TestStep.Builder().withName(TEST_STEP_2))
            .build()[0]
    }

}