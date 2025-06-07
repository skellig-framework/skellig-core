package org.skellig.plugin.report.model


import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.skellig.feature.event.Result

class TestScenarioReportDetailsTest {

    @Test
    fun `return total passed test steps`() {
        val mockedTestStepReportDetails = mock<TestStepReportDetails>()
        whenever(mockedTestStepReportDetails.isPassed()).thenReturn(true)

        val testScenarioReportDetails = TestScenarioReportDetails(
            name = "Test scenario",
            tags = setOf("tag1"),
            testStepReportDetails = mutableMapOf(Pair(0, mockedTestStepReportDetails))
        )

        assertEquals(1, testScenarioReportDetails.getTotalPassedTestSteps())
    }

    @Test
    fun `check if failed when at least one test step is failed`() {
        val mockedTestStepReportDetails1 = mock<TestStepReportDetails>()
        whenever(mockedTestStepReportDetails1.isPassed()).thenReturn(true)

        val mockedTestStepReportDetails2 = mock<TestStepReportDetails>()
        whenever(mockedTestStepReportDetails2.isPassed()).thenReturn(false)

        val testScenarioReportDetails = TestScenarioReportDetails(
            name = "Test scenario",
            tags = emptySet(),
            testStepReportDetails = mutableMapOf(Pair(1, mockedTestStepReportDetails1), Pair(2, mockedTestStepReportDetails2))
        )

        assertTrue(testScenarioReportDetails.isFailed())
    }

    @Test
    fun `return passed scenario`() {
        val mockedTestStepReportDetails = mock<TestStepReportDetails>()
        whenever(mockedTestStepReportDetails.isPassed()).thenReturn(true)

        val testScenarioReportDetails = TestScenarioReportDetails(
            name = "Test scenario",
            tags = setOf("tag1"),
            testStepReportDetails = mutableMapOf(Pair(0, mockedTestStepReportDetails))
        )

        assert(testScenarioReportDetails.isPassed())
    }

    @Test
    fun `return total test steps`() {
        val mockedTestStepReportDetails = mock<TestStepReportDetails>()

        val testScenarioReportDetails = TestScenarioReportDetails(
            name = "Test scenario",
            tags = setOf("tag1"),
            testStepReportDetails = mutableMapOf(Pair(0, mockedTestStepReportDetails))
        )

        assertEquals(1, testScenarioReportDetails.getTotalTestSteps())
    }

    @Test
    fun `return tags as string when tags are not null`() {
        val testScenarioReportDetails = TestScenarioReportDetails(
            name = "Test scenario",
            tags = setOf("tag1", "tag2")
        )

        assertEquals("tag1 tag2", testScenarioReportDetails.getTagsLine())
    }

    @Test
    fun `return null as string when tags are null`() {
        val testScenarioReportDetails = TestScenarioReportDetails(
            name = "Test scenario",
            tags = null
        )

        assertEquals(null, testScenarioReportDetails.getTagsLine())
    }

    @Test
    fun `return total failed test steps`() {
        val mockedTestStepReportDetails1 = mock<TestStepReportDetails>()
        whenever(mockedTestStepReportDetails1.isPassed()).thenReturn(true)

        val mockedTestStepReportDetails2 = mock<TestStepReportDetails>()
        whenever(mockedTestStepReportDetails2.isPassed()).thenReturn(false)

        val testScenarioReportDetails = TestScenarioReportDetails(
            name = "Test scenario",
            tags = setOf("tag1"),
            testStepReportDetails = mutableMapOf(Pair(0, mockedTestStepReportDetails1), Pair(1, mockedTestStepReportDetails2))
        )

        assertEquals(1, testScenarioReportDetails.getTotalFailedTestSteps())
    }

    @Test
    fun `return total passed percentage`() {
        val mockedTestStepReportDetails1 = mock<TestStepReportDetails>()
        whenever(mockedTestStepReportDetails1.isPassed()).thenReturn(true)

        val mockedTestStepReportDetails2 = mock<TestStepReportDetails>()
        whenever(mockedTestStepReportDetails2.isPassed()).thenReturn(false)

        val testScenarioReportDetails = TestScenarioReportDetails(
            name = "Test scenario",
            tags = setOf("tag1"),
            testStepReportDetails = mutableMapOf(Pair(0, mockedTestStepReportDetails1), Pair(1, mockedTestStepReportDetails2))
        )

        assertEquals(50.0f, testScenarioReportDetails.getTotalPassedPercentage())
    }

    @Test
    fun `return total passed percentage if no steps recorded`() {
        val testScenarioReportDetails = TestScenarioReportDetails(
            name = "Test scenario",
            tags = emptySet(),
            testStepReportDetails = mutableMapOf()
        )

        assertEquals(0.0f, testScenarioReportDetails.getTotalPassedPercentage())
    }

    @Test
    fun `return formatted scenario duration`() {
        val mockedTestStepReportDetails1 = mock<TestStepReportDetails>()
        whenever(mockedTestStepReportDetails1.duration).thenReturn(1000) // 1 second

        val mockedTestStepReportDetails2 = mock<TestStepReportDetails>()
        whenever(mockedTestStepReportDetails2.duration).thenReturn(2000) // 2 seconds

        val mockedTestStepReportDetails3 = mock<TestStepReportDetails>()
        whenever(mockedTestStepReportDetails3.duration).thenReturn(15789) // Approx. 15.789 seconds

        val testScenarioReportDetails = TestScenarioReportDetails(
            name = "Test scenario",
            tags = setOf("tag1"),
            testStepReportDetails = mutableMapOf(Pair(0, mockedTestStepReportDetails1), Pair(1, mockedTestStepReportDetails2), Pair(2, mockedTestStepReportDetails3))
        )

        // Given that the `getFormattedDuration` function formats duration to 'min', 'sec', or 'ms' depending on its length,
        // the total duration here is 1sec + 2sec + 15.789sec = 18.789sec.
        // Hence, the expected formatted string is "18.789 sec."
        assertEquals("18.789 sec.", testScenarioReportDetails.getScenarioDurationFormatted())
    }

    @Test
    fun `return formatted scenario duration for before scenario`() {
        val mockedTestStepReportDetails1 = mock<TestStepReportDetails>()
        whenever(mockedTestStepReportDetails1.duration).thenReturn(1000) // 1 second

        val mockedTestStepReportDetails2 = mock<TestStepReportDetails>()
        whenever(mockedTestStepReportDetails2.duration).thenReturn(2000) // 2 seconds

        val testScenarioReportDetails = TestScenarioReportDetails(
            name = "Test scenario",
            tags = setOf("tag1"),
            beforeReportDetails = mutableMapOf(Pair(0, mockedTestStepReportDetails1), Pair(1, mockedTestStepReportDetails2)),
        )

        assertEquals("03.000 sec.", testScenarioReportDetails.getBeforeScenarioDurationFormatted())
    }

    @Test
    fun `return formatted scenario duration for after scenario`() {
        val mockedTestStepReportDetails1 = mock<TestStepReportDetails>()
        whenever(mockedTestStepReportDetails1.duration).thenReturn(5350) // 5 seconds 350 ms

        val mockedTestStepReportDetails2 = mock<TestStepReportDetails>()
        whenever(mockedTestStepReportDetails2.duration).thenReturn(2010) // 2 seconds 10 ms

        val testScenarioReportDetails = TestScenarioReportDetails(
            name = "Test scenario",
            tags = setOf("tag1"),
            afterReportDetails = mutableMapOf(Pair(0, mockedTestStepReportDetails1), Pair(1, mockedTestStepReportDetails2)),
        )

        assertEquals("07.360 sec.", testScenarioReportDetails.getAfterScenarioDurationFormatted())
    }

    @Test
    fun `return formatted scenario duration in minutes for after scenario`() {
        val mockedTestStepReportDetails1 = mock<TestStepReportDetails>()
        whenever(mockedTestStepReportDetails1.duration).thenReturn(1000 * 60) // 1 min

        val mockedTestStepReportDetails2 = mock<TestStepReportDetails>()
        whenever(mockedTestStepReportDetails2.duration).thenReturn(1000 * 30) // 30 seconds

        val testScenarioReportDetails = TestScenarioReportDetails(
            name = "Test scenario",
            tags = setOf("tag1"),
            afterReportDetails = mutableMapOf(Pair(0, mockedTestStepReportDetails1), Pair(1, mockedTestStepReportDetails2)),
        )

        assertEquals("01.30.000 min.", testScenarioReportDetails.getAfterScenarioDurationFormatted())
    }

    @Test
    fun `return formatted duration for before hooks`() {
        val mockedTestStepReportDetails1 = mock<HookReportDetails>()
        val result1 = mock<Result>()
        whenever(mockedTestStepReportDetails1.result).thenReturn(result1)
        whenever(mockedTestStepReportDetails1.result.duration).thenReturn(5000) // 5 seconds

        val mockedTestStepReportDetails2 = mock<HookReportDetails>()
        val result2 = mock<Result>()
        whenever(mockedTestStepReportDetails2.result).thenReturn(result2)
        whenever(mockedTestStepReportDetails2.result.duration).thenReturn(3000) // 3 seconds

        val testScenarioReportDetails = TestScenarioReportDetails(
            name = "Test scenario",
            tags = setOf("tag1"),
            beforeHooksReportDetails = mutableListOf(mockedTestStepReportDetails1, mockedTestStepReportDetails2),
        )

        assertEquals("08.000 sec.", testScenarioReportDetails.getBeforeHooksDurationFormatted())
    }

    @Test
    fun `return formatted duration for after hooks`() {
        val mockedTestStepReportDetails1 = mock<HookReportDetails>()
        val result1 = mock<Result>()
        whenever(mockedTestStepReportDetails1.result).thenReturn(result1)
        whenever(result1.duration).thenReturn(4000) // 4 seconds

        val mockedTestStepReportDetails2 = mock<HookReportDetails>()
        val result2 = mock<Result>()
        whenever(mockedTestStepReportDetails2.result).thenReturn(result2)
        whenever(result2.duration).thenReturn(6000) // 6 seconds

        val testScenarioReportDetails = TestScenarioReportDetails(
            name = "Test scenario",
            tags = setOf("tag1"),
            afterHooksReportDetails = mutableListOf(mockedTestStepReportDetails1, mockedTestStepReportDetails2),
        )

        assertEquals("10.000 sec.", testScenarioReportDetails.getAfterHooksDurationFormatted())
    }
}