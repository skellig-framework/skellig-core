package org.skellig.teststep.processing.model.factory

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.TestStep
import java.util.*

class GroupedTestStepFactoryTest {

    private val testStepRegistry = mock<TestStepRegistry>()
    private val testStepFactory = mock<TestStepFactory<TestStep>>()
    private val factory = GroupedTestStepFactory(testStepRegistry, testStepFactory, System.getProperties(), mock())

    @Test
    fun testCreateSimpleGroupedTestStep() {
        val rawTestStep2 = mock<Map<String, Any?>>()
        val rawTestStep3 = mock<Map<String, Any?>>()
        val rawTestStep4 = mock<Map<String, Any?>>()
        val rawTestStep5 = mock<Map<String, Any?>>()

        whenever(testStepRegistry.getByName(anyString()))
                .thenAnswer {
                    return@thenAnswer when (it.arguments[0]) {
                        "test 2" -> rawTestStep2
                        "test 3" -> rawTestStep3
                        "test 4" -> rawTestStep4
                        "test 5" -> rawTestStep5
                        else -> null
                    }
                }

        whenever(testStepFactory.create(eq("test 2"), eq(rawTestStep2), any())).thenReturn(DefaultTestStep(name = "test 2"))
        whenever(testStepFactory.create(eq("test 3"), eq(rawTestStep3), any())).thenReturn(DefaultTestStep(name = "test 3"))
        whenever(testStepFactory.create(eq("test 4"), eq(rawTestStep4), any())).thenReturn(DefaultTestStep(name = "test 4"))
        whenever(testStepFactory.create(eq("test 5"), eq(rawTestStep5), any())).thenReturn(DefaultTestStep(name = "test 5"))


        val testStepName = "test 1"
        val rawTestStep = mapOf<String, Any?>(
                Pair("name", testStepName),
                Pair("test", "test 2"),
                Pair("passed", mapOf(
                        Pair("test", "test 3")
                )),
                Pair("failed", mapOf(
                        Pair("test", "test 4"),
                        Pair("failed", mapOf(
                                Pair("test", "test 5")
                        )),
                ))
        )

        val testStep = factory.create(testStepName, rawTestStep, emptyMap())

        assertAll(
                { assertEquals(testStepName, testStep.name) },
                { assertEquals("test 2", testStep.testStepToRun.testStepLazy().name) },
                { assertEquals("test 3", testStep.testStepToRun.passed!!.testStepLazy().name) },
                { assertEquals("test 4", testStep.testStepToRun.failed!!.testStepLazy().name) },
                { assertEquals("test 5", testStep.testStepToRun.failed!!.failed!!.testStepLazy().name) },
        )
    }

    @Test
    fun testCreateGroupedTestStepWithSomeEmptyPassedOrFailedSections() {
        val rawTestStep2 = mock<Map<String, Any?>>()
        whenever(testStepRegistry.getByName(anyString())).thenReturn(rawTestStep2)
        whenever(testStepFactory.create(eq("test 2"), eq(rawTestStep2), any())).thenReturn(DefaultTestStep(name = "test 2"))

        val testStepName = "test 1"
        val rawTestStep = mapOf<String, Any?>(
                Pair("name", testStepName),
                Pair("test", "test 2"),
                Pair("passed", emptyMap<String, Any?>()),
                Pair("failed", emptyMap<String, Any?>())
        )

        val testStep = factory.create(testStepName, rawTestStep, emptyMap())

        assertAll(
                { assertEquals(testStepName, testStep.name) },
                { assertEquals("test 2", testStep.testStepToRun.testStepLazy().name) },
                { assertNull(testStep.testStepToRun.passed) },
                { assertNull(testStep.testStepToRun.failed) }
        )
    }
}