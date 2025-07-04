package org.skellig.plugin.report

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.skellig.feature.Feature
import org.skellig.feature.TestScenario
import org.skellig.feature.TestStep
import org.skellig.feature.event.*
import java.io.File

class SkelligToCucumberReportPluginTest {

    @Test
    fun testGenerateReport() {
        val eventDispatcher = DefaultSkelligTestEventDispatcher()
        val reportFile = "target/cucumber-test.json"
        SkelligToCucumberReportPlugin(reportFile).init(eventDispatcher)

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
                mapOf(Pair("a", "100")), ExecutionSequenceType.NORMAL
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

        val objectMapper = ObjectMapper()
        val report = objectMapper.readValue(File(reportFile), List::class.java)[0] as Map<*, *>
        val elements = (report["elements"] as List<*>)[0] as Map<*, *>
        val step1 = (elements["steps"] as List<*>)[0] as Map<*, *>
        val step2 = (elements["steps"] as List<*>)[1] as Map<*, *>

        assertAll(
            { assertEquals(feature.name, report["name"]) },
            { assertEquals(feature.filePath, report["uri"]) },
            { assertEquals("feature", report["type"]) },
            { assertEquals("Feature", report["keyword"]) },
            { assertEquals(listOf(mapOf(Pair("name", "@tag1")), mapOf(Pair("name", "@tag2"))), report["tags"]) },

            { assertEquals(1, (elements["before"] as List<*>).size) },
            { assertEquals("passed", ((((elements["before"] as List<*>)[0] as Map<*, *>)["result"]) as Map<*, *>)["status"]) },
            { assertEquals(1, (elements["after"] as List<*>).size) },

            { assertEquals(testScenario.name, elements["name"]) },
            { assertEquals("scenario", elements["type"]) },
            { assertEquals("Scenario", elements["keyword"]) },
            { assertEquals(2, (elements["steps"] as List<*>).size) },

            { assertEquals(testStep.name, step1["name"]) },
            { assertEquals("step", step1["type"]) },
            { assertEquals("", step1["keyword"]) },
            { assertEquals("passed", (step1["result"] as Map<*, *>)["status"]) },
            { assertEquals(1000000000, (step1["result"] as Map<*, *>)["duration"]) },
            { assertEquals("b", (((step2["rows"] as List<*>)[0] as Map<*, *>)["cells"] as List<*>)[0]) },

            { assertEquals("100", (((step2["rows"] as List<*>)[0] as Map<*, *>)["cells"] as List<*>)[1]) },
            { assertEquals(listOf("test step 2 logs"), step2["output"]) },
        )
    }

    @Test
    fun testGenerateReportWhenSomeEntitiesIgnored() {
        val eventDispatcher = DefaultSkelligTestEventDispatcher()
        val reportFile = "target/cucumber-test-2.json"
        SkelligToCucumberReportPlugin(reportFile).init(eventDispatcher)

        val result = Result(1000L, null, null)

        val feature = Feature.Builder()
            .withName("Test feature")
            .withFilePath("test.feature")
            .build()
        eventDispatcher.dispatch(FeatureStartedEvent(feature))

        val testScenario = TestScenario.Builder()
            .withName("Test scenario")
            .build().first()
        eventDispatcher.dispatch(TestScenarioStartedEvent(testScenario, 0))

        val testStep = TestStep.Builder()
            .withName("Test step")
            .build()
        eventDispatcher.dispatch(TestStepStartedEvent(testStep, 0, testScenario.getId(), emptyMap()))
        eventDispatcher.dispatch(TestStepStartedEvent(testStep, feature.getId(), null, null))
        eventDispatcher.dispatch(HookFinishedEvent(null, feature.getId(), testScenario.getId(), emptyList(), result))
        eventDispatcher.dispatch(HookFinishedEvent("hook1", 0, testScenario.getId(), null, result))
        eventDispatcher.dispatch(HookFinishedEvent("hook1", feature.getId(), 0, null, result))

        eventDispatcher.dispatch(
            TestStepFinishedEvent(
                testStep.getId(), 0, testScenario.getId(), TestExecutionStatus.PASSED,
                null, emptyMap<String, String>(), ExecutionSequenceType.NORMAL
            )
        )
        eventDispatcher.dispatch(
            TestStepFinishedEvent(
                testStep.getId(), feature.getId(), 0, TestExecutionStatus.PASSED,
                null, emptyMap<String, String>(), ExecutionSequenceType.NORMAL
            )
        )
        eventDispatcher.dispatch(
            TestStepProcessingFinishedEvent(
                testStep.getId(), 0, testScenario.getId(), result,
                emptyMap(), ExecutionSequenceType.NORMAL
            )
        )
        eventDispatcher.dispatch(
            TestStepProcessingFinishedEvent(
                testStep.getId(), feature.getId(), 0, result,
                emptyMap(), ExecutionSequenceType.NORMAL
            )
        )

        eventDispatcher.dispatch(TestRunFinishedEvent())

        val objectMapper = ObjectMapper()
        val report = objectMapper.readValue(File(reportFile), List::class.java)[0] as Map<*, *>

        assertAll(
            { assertEquals(feature.name, report["name"]) },
            { assertEquals(feature.filePath, report["uri"]) },
            { assertEquals("feature", report["type"]) },
            { assertEquals("Feature", report["keyword"]) },
            { assertEquals(emptyList<Any>(), report["tags"]) },
            { assertEquals(emptyList<Any>(), report["elements"]) }
        )
    }

    @Test
    fun testGetName() {
        assertEquals("SkelligToCucumberReport", SkelligToCucumberReportPlugin("").getName())
    }
}