package org.skellig.teststep.processor.unix

import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.skellig.teststep.processing.converter.TestStepResultConverter
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.TestStepResultValidator
import org.skellig.teststep.processor.unix.DefaultSshClient
import org.skellig.teststep.processor.unix.model.UnixShellTestStep
import java.util.*

internal class UnixShellTestStepProcessorTest {

    private var processor: UnixShellTestStepProcessor? = null
    private var sshClient = Mockito.mock(DefaultSshClient::class.java)
    private var sshClient2 = Mockito.mock(DefaultSshClient::class.java)

    @BeforeEach
    fun setUp() {
        val hosts = mapOf(
                Pair("h1", sshClient),
                Pair("h2", sshClient2))

        processor = UnixShellTestStepProcessor(Mockito.mock(TestScenarioState::class.java),
                Mockito.mock(TestStepResultValidator::class.java), Mockito.mock(TestStepResultConverter::class.java), hosts)
    }

    @Test
    fun testRunCommandToNoHost() {
        val testStep = UnixShellTestStep.Builder()
                .withHosts(emptyList())
                .withCommand("ls")
                .withName("n1")
                .build() as UnixShellTestStep

        processor!!.process(testStep)
                .subscribe { _, _, e ->
                    Assertions.assertEquals("No hosts were provided to run a command." +
                            " Registered hosts are: [h1, h2]", e!!.message)
                }
    }

    @Test
    fun testRunCommandToNonRegisteredHost() {
        val testStep = UnixShellTestStep.Builder()
                .withHosts(listOf("h3"))
                .withCommand("ls")
                .withName("n1")
                .build() as UnixShellTestStep

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
                .build() as UnixShellTestStep
        whenever(sshClient!!.runShellCommand(testStep.getCommand(), testStep.timeout)).thenReturn("r1")

        processor!!.process(testStep)
                .subscribe { _, r, _ ->
                    val result = r as Map<*, *>
                    Assertions.assertEquals(1, result.size)
                    Assertions.assertEquals("r1", result["h1"])
                }
        Mockito.verifyZeroInteractions(sshClient2)
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
                .build() as UnixShellTestStep
        whenever(sshClient!!.runShellCommand(testStep.getCommand(), testStep.timeout)).thenReturn("r1")
        whenever(sshClient2!!.runShellCommand(testStep.getCommand(), testStep.timeout)).thenReturn("r2")

        processor!!.process(testStep)
                .subscribe { _, r, _ ->
                    val result = r as Map<*, *>
                    Assertions.assertEquals("r1", result["h1"])
                    Assertions.assertEquals("r2", result["h2"])
                }
    }
}