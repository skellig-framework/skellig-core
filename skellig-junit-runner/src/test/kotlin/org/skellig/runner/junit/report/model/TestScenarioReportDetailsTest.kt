package org.skellig.runner.junit.report.model

import org.junit.Assert.*
import org.junit.Test


import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class TestScenarioReportDetailsTest {

    @Test
    fun `return total passed test steps`() {
        val mockedTestStepReportDetails = mock<TestStepReportDetails<*>>()
        whenever(mockedTestStepReportDetails.isPassed()).thenReturn(true)

        val testScenarioReportDetails = TestScenarioReportDetails(
            name = "Test scenario",
            tags = setOf("tag1"),
            beforeHooksReportDetails = null,
            afterHooksReportDetails = null,
            beforeReportDetails = null,
            afterReportDetails = null,
            testStepReportDetails = listOf(mockedTestStepReportDetails)
        )

        assertEquals(1, testScenarioReportDetails.getTotalPassedTestSteps())
    }

    @Test
    fun `return passed scenario`() {
        val mockedTestStepReportDetails = mock<TestStepReportDetails<*>>()
        whenever(mockedTestStepReportDetails.isPassed()).thenReturn(true)

        val testScenarioReportDetails = TestScenarioReportDetails(
            name = "Test scenario",
            tags = setOf("tag1"),
            beforeHooksReportDetails = null,
            afterHooksReportDetails = null,
            beforeReportDetails = null,
            afterReportDetails = null,
            testStepReportDetails = listOf(mockedTestStepReportDetails)
        )

        assert(testScenarioReportDetails.isPassed())
    }

    @Test
    fun `return total test steps`() {
        val mockedTestStepReportDetails = mock<TestStepReportDetails<*>>()

        val testScenarioReportDetails = TestScenarioReportDetails(
            name = "Test scenario",
            tags = setOf("tag1"),
            beforeHooksReportDetails = null,
            afterHooksReportDetails = null,
            beforeReportDetails = null,
            afterReportDetails = null,
            testStepReportDetails = listOf(mockedTestStepReportDetails)
        )

        assertEquals(1, testScenarioReportDetails.getTotalTestSteps())
    }

    @Test
    fun `return tags as string when tags are not null`() {
        val testScenarioReportDetails = TestScenarioReportDetails(
            name = "Test scenario",
            tags = setOf("tag1", "tag2"),
            beforeHooksReportDetails = null,
            afterHooksReportDetails = null,
            beforeReportDetails = null,
            afterReportDetails = null,
            testStepReportDetails = null
        )

        assertEquals("tag1 tag2", testScenarioReportDetails.getTagsLine())
    }

    @Test
    fun `return null as string when tags are null`() {
        val testScenarioReportDetails = TestScenarioReportDetails(
            name = "Test scenario",
            tags = null,
            beforeHooksReportDetails = null,
            afterHooksReportDetails = null,
            beforeReportDetails = null,
            afterReportDetails = null,
            testStepReportDetails = null
        )

        assertEquals(null, testScenarioReportDetails.getTagsLine())
    }

    @Test
    fun `return total failed test steps`() {
        val mockedTestStepReportDetails1 = mock<TestStepReportDetails<*>>()
        whenever(mockedTestStepReportDetails1.isPassed()).thenReturn(true)

        val mockedTestStepReportDetails2 = mock<TestStepReportDetails<*>>()
        whenever(mockedTestStepReportDetails2.isPassed()).thenReturn(false)

        val testScenarioReportDetails = TestScenarioReportDetails(
            name = "Test scenario",
            tags = setOf("tag1"),
            beforeHooksReportDetails = null,
            afterHooksReportDetails = null,
            beforeReportDetails = null,
            afterReportDetails = null,
            testStepReportDetails = listOf(mockedTestStepReportDetails1, mockedTestStepReportDetails2)
        )

        assertEquals(1, testScenarioReportDetails.getTotalFailedTestSteps())
    }

    @Test
    fun `return total passed percentage`() {
        val mockedTestStepReportDetails1 = mock<TestStepReportDetails<*>>()
        whenever(mockedTestStepReportDetails1.isPassed()).thenReturn(true)

        val mockedTestStepReportDetails2 = mock<TestStepReportDetails<*>>()
        whenever(mockedTestStepReportDetails2.isPassed()).thenReturn(false)

        val testScenarioReportDetails = TestScenarioReportDetails(
            name = "Test scenario",
            tags = setOf("tag1"),
            beforeHooksReportDetails = null,
            afterHooksReportDetails = null,
            beforeReportDetails = null,
            afterReportDetails = null,
            testStepReportDetails = listOf(mockedTestStepReportDetails1, mockedTestStepReportDetails2)
        )

        assertEquals(50.0f, testScenarioReportDetails.getTotalPassedPercentage())
    }

    @Test
    fun `return formatted scenario duration`() {
        val mockedTestStepReportDetails1 = mock<TestStepReportDetails<*>>()
        whenever(mockedTestStepReportDetails1.duration).thenReturn(1000) // 1 second

        val mockedTestStepReportDetails2 = mock<TestStepReportDetails<*>>()
        whenever(mockedTestStepReportDetails2.duration).thenReturn(2000) // 2 seconds

        val mockedTestStepReportDetails3 = mock<TestStepReportDetails<*>>()
        whenever(mockedTestStepReportDetails3.duration).thenReturn(15789) // Approx. 15.789 seconds

        val testScenarioReportDetails = TestScenarioReportDetails(
            name = "Test scenario",
            tags = setOf("tag1"),
            beforeHooksReportDetails = null,
            afterHooksReportDetails = null,
            beforeReportDetails = null,
            afterReportDetails = null,
            testStepReportDetails = listOf(mockedTestStepReportDetails1, mockedTestStepReportDetails2, mockedTestStepReportDetails3)
        )

        // Given that the `getFormattedDuration` function formats duration to 'min', 'sec', or 'ms' depending on its length,
        // the total duration here is 1sec + 2sec + 15.789sec = 18.789sec.
        // Hence, the expected formatted string is "18.789 sec."
        assertEquals("18.789 sec.", testScenarioReportDetails.getScenarioDurationFormatted())
    }

    @Test
    fun `return formatted scenario duration for before scenario`() {
        val mockedTestStepReportDetails1 = mock<TestStepReportDetails<*>>()
        whenever(mockedTestStepReportDetails1.duration).thenReturn(1000) // 1 second

        val mockedTestStepReportDetails2 = mock<TestStepReportDetails<*>>()
        whenever(mockedTestStepReportDetails2.duration).thenReturn(2000) // 2 seconds

        val testScenarioReportDetails = TestScenarioReportDetails(
            name = "Test scenario",
            tags = setOf("tag1"),
            beforeHooksReportDetails = null,
            afterHooksReportDetails = null,
            beforeReportDetails = listOf(mockedTestStepReportDetails1, mockedTestStepReportDetails2),
            afterReportDetails = null,
            testStepReportDetails = null
        )

        assertEquals("03.000 sec.", testScenarioReportDetails.getBeforeScenarioDurationFormatted())
    }

    @Test
    fun `return formatted scenario duration for after scenario`() {
        val mockedTestStepReportDetails1 = mock<TestStepReportDetails<*>>()
        whenever(mockedTestStepReportDetails1.duration).thenReturn(5350) // 5 seconds 350 ms

        val mockedTestStepReportDetails2 = mock<TestStepReportDetails<*>>()
        whenever(mockedTestStepReportDetails2.duration).thenReturn(2010) // 2 seconds 10 ms

        val testScenarioReportDetails = TestScenarioReportDetails(
            name = "Test scenario",
            tags = setOf("tag1"),
            beforeHooksReportDetails = null,
            afterHooksReportDetails = null,
            beforeReportDetails = null,
            afterReportDetails = listOf(mockedTestStepReportDetails1, mockedTestStepReportDetails2),
            testStepReportDetails = null
        )

        assertEquals("07.360 sec.", testScenarioReportDetails.getAfterScenarioDurationFormatted())
    }

    @Test
    fun `return formatted scenario duration in minutes for after scenario`() {
        val mockedTestStepReportDetails1 = mock<TestStepReportDetails<*>>()
        whenever(mockedTestStepReportDetails1.duration).thenReturn(1000 * 60) // 1 min

        val mockedTestStepReportDetails2 = mock<TestStepReportDetails<*>>()
        whenever(mockedTestStepReportDetails2.duration).thenReturn(1000 * 30) // 30 seconds

        val testScenarioReportDetails = TestScenarioReportDetails(
            name = "Test scenario",
            tags = setOf("tag1"),
            beforeHooksReportDetails = null,
            afterHooksReportDetails = null,
            beforeReportDetails = null,
            afterReportDetails = listOf(mockedTestStepReportDetails1, mockedTestStepReportDetails2),
            testStepReportDetails = null
        )

        assertEquals("01.30.000 min.", testScenarioReportDetails.getAfterScenarioDurationFormatted())
    }

    @Test
    fun `return formatted duration for before hooks`() {
        val mockedTestStepReportDetails1 = mock<HookReportDetails>()
        whenever(mockedTestStepReportDetails1.duration).thenReturn(5000) // 5 seconds

        val mockedTestStepReportDetails2 = mock<HookReportDetails>()
        whenever(mockedTestStepReportDetails2.duration).thenReturn(3000) // 3 seconds

        val testScenarioReportDetails = TestScenarioReportDetails(
            name = "Test scenario",
            tags = setOf("tag1"),
            beforeHooksReportDetails = listOf(mockedTestStepReportDetails1, mockedTestStepReportDetails2),
            afterHooksReportDetails = null,
            beforeReportDetails = null,
            afterReportDetails = null,
            testStepReportDetails = null
        )

        assertEquals("08.000 sec.", testScenarioReportDetails.getBeforeHooksDurationFormatted())
    }

    @Test
    fun `return formatted duration for after hooks`() {
        val mockedTestStepReportDetails1 = mock<HookReportDetails>()
        whenever(mockedTestStepReportDetails1.duration).thenReturn(4000) // 4 seconds

        val mockedTestStepReportDetails2 = mock<HookReportDetails>()
        whenever(mockedTestStepReportDetails2.duration).thenReturn(6000) // 6 seconds

        val testScenarioReportDetails = TestScenarioReportDetails(
            name = "Test scenario",
            tags = setOf("tag1"),
            beforeHooksReportDetails = null,
            afterHooksReportDetails = listOf(mockedTestStepReportDetails1, mockedTestStepReportDetails2),
            beforeReportDetails = null,
            afterReportDetails = null,
            testStepReportDetails = null
        )

        assertEquals("10.000 sec.", testScenarioReportDetails.getAfterHooksDurationFormatted())
    }
}