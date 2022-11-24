package org.skellig.teststep.processor.unix

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.skellig.teststep.processor.unix.model.UnixShellTestStep

internal class UnixShellTestStepProcessorTest {

    private var processor: UnixShellTestStepProcessor? = null
    private var sshClient = mock<DefaultSshClient>()
    private var sshClient2 = mock<DefaultSshClient>()

    @BeforeEach
    fun setUp() {
        val hosts = mapOf(
            Pair("h1", sshClient),
            Pair("h2", sshClient2))

        processor = UnixShellTestStepProcessor(mock(), mock(), hosts)
    }

    @Test
    fun testRunCommandToNoHost() {
        val testStep = UnixShellTestStep.Builder()
            .withHosts(emptyList())
            .withCommand("ls")
            .withName("n1")
            .build()

        processor!!.process(testStep)
            .subscribe { _, _, e ->
                Assertions.assertEquals("No hosts were provided to run a command." +
                                                " Registered hosts are: [h1, h2]", e!!.message)
            }
    }

    @Test
    fun testRunCommandToNoHostWhenOneWasRegistered() {
        val hosts = mapOf(Pair("h1", sshClient))

        processor = UnixShellTestStepProcessor(mock(), mock(), hosts)

        val testStep = UnixShellTestStep.Builder()
            .withCommand("ls")
            .withName("n1")
            .build()

        whenever(sshClient.runShellCommand(testStep.getCommand(), testStep.timeout)).thenReturn("r1")

        processor!!.process(testStep)
            .subscribe { _, r, _ ->
                Assertions.assertEquals("r1", r)
            }
    }

    @Test
    fun testRunCommandToNonRegisteredHost() {
        val testStep = UnixShellTestStep.Builder()
            .withHosts(listOf("h3"))
            .withCommand("ls")
            .withName("n1")
            .build()

        processor!!.process(testStep)
            .subscribe { _, _, e ->
                Assertions.assertEquals("No hosts was registered for host name 'h3'." +
                                                " Registered hosts are: [h1, h2]", e!!.message)
            }
    }

    @Test
    fun testRunCommand() {
        val testStep = UnixShellTestStep.Builder()
            .withHosts(listOf("h1"))
            .withCommand("ls -l")
            .withName("n1")
            .build()
        whenever(sshClient.runShellCommand(testStep.getCommand(), testStep.timeout)).thenReturn("r1")

        processor!!.process(testStep)
            .subscribe { _, r, _ ->
                val result = r as Map<*, *>
                Assertions.assertEquals(1, result.size)
                Assertions.assertEquals("r1", result["h1"])
            }
        Mockito.verifyNoInteractions(sshClient2)
    }

    @Test
    fun testRunCommandWithArguments() {
        val args: MutableMap<String, String> = HashMap()
        args["a1"] = "v1"
        args["a2"] = "v2"
        val testStep = UnixShellTestStep.Builder()
            .withHosts(listOf("h1", "h2"))
            .withCommand("cmd1")
            .withArgs(args)
            .withName("n1")
            .build()
        whenever(sshClient.runShellCommand(testStep.getCommand(), testStep.timeout)).thenReturn("r1")
        whenever(sshClient2.runShellCommand(testStep.getCommand(), testStep.timeout)).thenReturn("r2")

        processor!!.process(testStep)
            .subscribe { _, r, _ ->
                val result = r as Map<*, *>
                Assertions.assertEquals("r1", result["h1"])
                Assertions.assertEquals("r2", result["h2"])
            }
    }
}