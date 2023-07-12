package org.skellig.runner.junit.report.attachment.log.fromfile

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.transport.verification.HostKeyVerifier
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.skellig.runner.junit.report.attachment.log.fromfile.ExtractRemoteLogRecordsUnixCommandRunner.Companion.DEFAULT_CONNECT_TIMEOUT
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.StringBufferInputStream
import java.io.StringReader
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit

class ExtractRemoteLogRecordsUnixCommandRunnerTest {

    val session: Session = mock()

    @Test
    fun testGetLogRecords() {
        val logExtractionDetails =
            LogExtractionDetails.Builder()
                .withMaxRecords(10)
                .withPath("path")
                .build()
        val commandRunner = ExtractRemoteLogRecordsUnixCommandRunnerForTest(logExtractionDetails)

        val command = mock<Session.Command>()
        whenever(session.exec("tail -n${logExtractionDetails.maxRecords} ${logExtractionDetails.path}")).thenReturn(command)

        val expectedLogRecords = "some records"
        val inputStream = ByteArrayInputStream(expectedLogRecords.toByteArray())
        whenever(command.inputStream).thenReturn(inputStream)

        val logRecords = commandRunner.getLogRecords()

        assertAll(
            { assertEquals(expectedLogRecords, logRecords) },
            { verify(session).allocateDefaultPTY() },
            { verify(command).join(DEFAULT_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS) }
        )
    }

    @Test
    fun testFailedToGetLogRecords() {
        val logExtractionDetails =
            LogExtractionDetails.Builder()
                .withPath("path")
                .build()
        val commandRunner = ExtractRemoteLogRecordsUnixCommandRunnerForTest(logExtractionDetails)

        whenever(session.exec(any())).thenThrow(RuntimeException("oops"))

        val logRecords = commandRunner.getLogRecords()

        assertEquals("Failed to get data from log file '${logExtractionDetails.path}': oops", logRecords)
    }

    @Test
    fun testConnectWithSshKey() {
        val logExtractionDetails =
            LogExtractionDetails.Builder()
                .withSshKeyPath("key path")
                .build()
        val commandRunner = ExtractRemoteLogRecordsUnixCommandRunnerForTest(logExtractionDetails)

        commandRunner.getLogRecords()

        commandRunner.verifyConnectionWithKeyMethodsCalled()
    }

    @Test
    fun testConnectWithPassword() {
        val logExtractionDetails =
            LogExtractionDetails.Builder()
                .withPassword("password")
                .build()
        val commandRunner = ExtractRemoteLogRecordsUnixCommandRunnerForTest(logExtractionDetails)

        commandRunner.getLogRecords()

        commandRunner.verifyConnectionWithPasswordMethodsCalled()
    }

    internal inner class ExtractRemoteLogRecordsUnixCommandRunnerForTest(logExtractionDetails: LogExtractionDetails) : ExtractRemoteLogRecordsUnixCommandRunner(logExtractionDetails) {

        private var methodsCalled = mutableSetOf<String>()

        override fun connect(hostname: String?, port: Int) {
            methodsCalled.add("connect")
        }

        override fun authPublickey(username: String?, vararg locations: String?) {
            methodsCalled.add("authPublickey")
        }

        override fun authPassword(username: String?, password: String?) {
            methodsCalled.add("authPassword")
        }

        override fun addHostKeyVerifier(verifier: HostKeyVerifier?) {
            if (verifier?.verify("", 0, mock()) == true)
                methodsCalled.add("addHostKeyVerifier")
        }

        override fun startSession(): Session {
            return session
        }

        fun verifyConnectionWithKeyMethodsCalled() {
            assertTrue(methodsCalled.contains("connect"))
            assertTrue(methodsCalled.contains("authPublickey"))
            assertTrue(methodsCalled.contains("addHostKeyVerifier"))
        }

        fun verifyConnectionWithPasswordMethodsCalled() {
            assertTrue(methodsCalled.contains("connect"))
            assertTrue(methodsCalled.contains("authPassword"))
        }
    }
}