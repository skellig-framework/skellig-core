package org.skellig.plugin.report

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.skellig.feature.Feature
import org.skellig.feature.TestScenario
import org.skellig.feature.TestStep
import org.skellig.feature.event.*
import java.io.File

class SkelligReportPluginTest {

    val reportDir = "report/tmp"

    @AfterEach
    fun tearDown() {
        // Clean up any created files
        File("target/$reportDir").listFiles()?.forEach { file ->
            if (file.isDirectory) {
                file.deleteRecursively()
            }
        }
    }

    @Test
    fun testGenerateReport() {
        val eventDispatcher = DefaultSkelligTestEventDispatcher()
        SkelligReportPlugin(reportDir).init(eventDispatcher)

        val feature = Feature.Builder()
            .withName("Test feature")
            .withFilePath("test.feature")
            .withTags(setOf("@tag1", "@tag2"))
            .build()
        eventDispatcher.dispatch(FeatureStartedEvent(feature))

        val testScenario = TestScenario.Builder()
            .withName("Test scenario")
            .build().first()
        eventDispatcher.dispatch(TestScenarioStartedEvent(testScenario, feature.getId()))

        val testStep = TestStep.Builder()
            .withName("Test step")
            .build()
        eventDispatcher.dispatch(TestStepStartedEvent(testStep, feature.getId(), testScenario.getId(), null))

        eventDispatcher.dispatch(HookFinishedEvent("hook1", feature.getId(), testScenario.getId(), listOf("hook logs"), Result(1000L, null, null)))
        eventDispatcher.dispatch(
            HookFinishedEvent(
                "hook before", feature.getId(), testScenario.getId(), null,
                Result(1000L, null, null), ExecutionSequenceType.BEFORE
            )
        )
        eventDispatcher.dispatch(
            HookFinishedEvent(
                "hook after", feature.getId(), testScenario.getId(), null,
                Result(1000L, null, null), ExecutionSequenceType.AFTER
            )
        )

        eventDispatcher.dispatch(
            TestStepFinishedEvent(
                testStep.getId(), feature.getId(), testScenario.getId(), ExecutionSequenceType.NORMAL
            )
        )

        val testStep2 = TestStep.Builder()
            .withName("Test step 2")
            .withPosition(2)
            .build()
        eventDispatcher.dispatch(TestStepStartedEvent(testStep2, feature.getId(), testScenario.getId(), mapOf(Pair("b", "100"))))
        eventDispatcher.dispatch(
            TestStepFinishedEvent(
                testStep2.getId(), feature.getId(), testScenario.getId(), TestExecutionStatus.PASSED,
                listOf("test step 2 logs"), emptyMap<String, String>(), ExecutionSequenceType.NORMAL
            )
        )

        eventDispatcher.dispatch(
            TestStepProcessingFinishedEvent(
                testStep.getId(), feature.getId(), testScenario.getId(), Result(1000L, null, null),
                mapOf(
                    Pair("Properties", "properties of ${testStep.name}"),
                    Pair("Test Data", "test data of ${testStep.name}"),
                    Pair("Validation Details", "validations of ${testStep.name}"),
                ),
                ExecutionSequenceType.NORMAL
            )
        )

        // cover the line which prevents step state update if not found by id
        eventDispatcher.dispatch(
            TestStepProcessingFinishedEvent(
                -1, feature.getId(), testScenario.getId(), Result(1000L, null, null),
                emptyMap(), ExecutionSequenceType.NORMAL
            )
        )

        eventDispatcher.dispatch(TestRunFinishedEvent())

        val reportFile = File("target/$reportDir/skellig-report/feature-reports/Test feature.html")
        assertTrue(reportFile.exists(), "Report file was not created")

        val content = reportFile.readText()

        listOf(
            feature.name,
            testScenario.name,
            testStep.name,
            testStep2.name,
            "properties of ${testStep.name}",
            "test data of ${testStep.name}",
            "validations of ${testStep.name}",
        ).parallelStream().forEach { assertTrue(content.contains(it)) }
    }

    @Test
    fun testGenerateReportWhenScenariosOfOneFeatureFinishConcurrently() {
        val eventDispatcher = DefaultSkelligTestEventDispatcher()
        SkelligReportPlugin(reportDir).init(eventDispatcher)

        val feature = Feature.Builder()
            .withName("Concurrent feature")
            .withFilePath("concurrent.feature")
            .build()
        eventDispatcher.dispatch(FeatureStartedEvent(feature))

        val scenarioCount = 16
        val stepsPerScenario = 20
        val pool = java.util.concurrent.Executors.newFixedThreadPool(scenarioCount)
        val start = java.util.concurrent.CountDownLatch(1)
        val futures = (0 until scenarioCount).map { s ->
            pool.submit {
                val scenario = TestScenario.Builder().withName("Scenario #$s#").build().first()
                start.await()
                eventDispatcher.dispatch(TestScenarioStartedEvent(scenario, feature.getId()))
                (0 until stepsPerScenario).forEach { i ->
                    val step = TestStep.Builder().withName("Step #$s-$i#").withPosition(i).build()
                    eventDispatcher.dispatch(TestStepStartedEvent(step, feature.getId(), scenario.getId(), null))
                    eventDispatcher.dispatch(
                        HookFinishedEvent("hook $s-$i", feature.getId(), scenario.getId(), null, Result(1L, null, null), ExecutionSequenceType.BEFORE)
                    )
                    eventDispatcher.dispatch(
                        TestStepProcessingFinishedEvent(
                            step.getId(), feature.getId(), scenario.getId(), Result(1L, null, null), emptyMap(), ExecutionSequenceType.NORMAL
                        )
                    )
                }
            }
        }
        start.countDown()
        futures.forEach { it.get(30, java.util.concurrent.TimeUnit.SECONDS) }
        pool.shutdownNow()

        eventDispatcher.dispatch(TestRunFinishedEvent())

        val content = File("target/$reportDir/skellig-report/feature-reports/Concurrent feature.html").readText()
        (0 until scenarioCount).forEach { s ->
            assertTrue(content.contains("Scenario #$s#"), "Scenario $s is missing in the report")
            (0 until stepsPerScenario).forEach { i -> assertTrue(content.contains("Step #$s-$i#"), "Step $s-$i is missing in the report") }
        }
    }
}
