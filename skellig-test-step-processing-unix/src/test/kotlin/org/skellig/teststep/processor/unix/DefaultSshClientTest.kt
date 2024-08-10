package org.skellig.teststep.processor.unix

import net.schmizz.sshj.connection.channel.direct.Session
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.skellig.teststep.processing.exception.TestStepProcessingException
import java.io.ByteArrayInputStream

class DefaultSshClientTest : DefaultSshClient("local", 1000, "user", null, "key") {

    private val sshSession = mock<Session>()
    private var isConnectFails = false

    @Test
    fun `run command`() {
        val command = "cmd"
        val commandResponse = mock<Session.Command>()
        whenever(commandResponse.inputStream).thenReturn(ByteArrayInputStream("response".toByteArray()))
        whenever(sshSession.exec(command)).thenReturn(commandResponse)

        assertEquals("response", runShellCommand(command, 0))
    }

    @Test
    fun `run command when fails`() {
        doThrow(RuntimeException("command failed")).whenever(sshSession).exec("cmd")

        assertEquals("", runShellCommand("cmd", 0))
    }

    @Test
    fun `close session`() {
        runShellCommand("cmd", 0)

        close()

        verify(sshSession).close()
    }

    @Test
    fun `close session when fails then verify it is ignored`() {
        runShellCommand("cmd", 0)
        doThrow(RuntimeException("close failed")).whenever(sshSession).close()

        close()

        verify(sshSession).close()
    }

    @Test
    fun `close session when not initialized`() {
        close()

        verifyNoInteractions(sshSession)
    }

    @Test
    fun `run command when session can't be started`() {
        isConnectFails = true
        val ex = assertThrows<TestStepProcessingException> { runShellCommand("cmd", 0) }

        assertEquals("connect fails", ex.message)
    }

    @Test
    fun `run command twice and verify session created only once`() {
        runShellCommand("cmd", 0)

        whenever(sshSession.isOpen).thenReturn(true)
        runShellCommand("cmd 2", 0)

        verify(sshSession, times(1)).allocateDefaultPTY()
    }

    @Nested
    inner class PasswordBasedDefaultSshClientTest : DefaultSshClient("local", 1000, "user", "pswd1", null) {

        private var isPasswordChecked = false

        @Test
        fun `run command and verify connected with password`() {
            runShellCommand("cmd", 0)

            assertTrue(isPasswordChecked, "Password must be checked")
        }

        override fun startSession(): Session {
            return sshSession
        }

        override fun authPassword(username: String?, password: String?) {
            assertEquals("user", username)
            assertEquals("pswd1", password)
            isPasswordChecked = true
        }

        override fun authPublickey(username: String?, vararg locations: String?) {
            throw RuntimeException("must not be called")
        }

        override fun addHostKeyVerifier(fingerprint: String?) {
        }

        override fun connect(hostname: String?, port: Int) {
        }
    }

    override fun startSession(): Session {
        return sshSession
    }

    override fun addHostKeyVerifier(fingerprint: String?) {
        assertEquals("", fingerprint)
    }

    override fun connect(hostname: String?, port: Int) {
        if (!isConnectFails) {
            assertEquals("local", hostname)
            assertEquals(1000, port)
        } else {
            throw RuntimeException("connect fails")
        }
    }

    override fun authPublickey(username: String?, vararg locations: String?) {
        assertEquals("user", user)
        assertEquals("key", locations[0])
    }
}