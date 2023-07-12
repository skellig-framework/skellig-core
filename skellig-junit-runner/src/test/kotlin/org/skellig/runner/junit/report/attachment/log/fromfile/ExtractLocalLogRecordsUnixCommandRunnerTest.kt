package org.skellig.runner.junit.report.attachment.log.fromfile

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.lang.RuntimeException

class ExtractLocalLogRecordsUnixCommandRunnerTest {

    @Test
    fun testLogRecords() {
        val logExtractionDetails =
            LogExtractionDetails.Builder()
                .withMaxRecords(100)
                .withPath("a/b/c")
                .build()

        val commandRunner = ExtractLocalLogRecordsUnixCommandRunnerForTest(logExtractionDetails)
        val expectedLogRecords = "some records"
        val inputStream = ByteArrayInputStream(expectedLogRecords.toByteArray())
        val process = mock<Process>()
        whenever(process.inputStream).thenReturn(inputStream)
        whenever(commandRunner.runtime.exec("tail -n${logExtractionDetails.maxRecords} ${logExtractionDetails.path}")).thenReturn(process)

        assertEquals(expectedLogRecords, commandRunner.getLogRecords())
    }

    @Test
    fun testLogRecordsWhenFails() {
        val logExtractionDetails =
            LogExtractionDetails.Builder()
                .withPath("a/b/c")
                .build()

        val commandRunner = ExtractLocalLogRecordsUnixCommandRunnerForTest(logExtractionDetails)
        whenever(commandRunner.runtime.exec(any<String>())).thenThrow(RuntimeException("oops"))

        assertEquals( "Failed to extract log records from file '${logExtractionDetails.path}': oops", commandRunner.getLogRecords())
    }

    internal inner class ExtractLocalLogRecordsUnixCommandRunnerForTest(logExtractionDetails: LogExtractionDetails) : ExtractLocalLogRecordsUnixCommandRunner(logExtractionDetails) {

        val runtime: Runtime = mock()

        override fun createRuntime(): Runtime {
            return runtime
        }
    }
}