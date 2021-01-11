package org.skellig.teststep.processor.unix

import com.typesafe.config.Config
import org.skellig.teststep.processing.converter.TestStepResultConverter
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.TestStepResultValidator
import org.skellig.teststep.processor.unix.model.UnixShellHostDetails
import org.skellig.teststep.processor.unix.model.UnixShellTestStep
import java.util.stream.Collectors

open class UnixShellTestStepProcessor(testScenarioState: TestScenarioState,
                                      validator: TestStepResultValidator,
                                      testStepResultConverter: TestStepResultConverter?,
                                      private val hosts: Map<String, DefaultSshClient>)
    : BaseTestStepProcessor<UnixShellTestStep>(testScenarioState, validator, testStepResultConverter) {

    protected override fun processTestStep(testStep: UnixShellTestStep): Any? {
        if (testStep.hosts.isEmpty()) {
            throw TestStepProcessingException("No hosts were provided to run a command." +
                    " Registered hosts are: " + hosts.keys.toString())
        }

        return testStep.hosts.parallelStream()
                .collect(Collectors.toMap({ it },
                        {
                            val sshClient = getDefaultSshClient(it)
                            sshClient!!.runShellCommand(testStep.getCommand(), testStep.timeout)
                        }))
    }

    private fun getDefaultSshClient(host: String): DefaultSshClient? {
        if (!hosts.containsKey(host)) {
            throw TestStepProcessingException(String.format("No hosts was registered for host name '%s'." +
                    " Registered hosts are: %s", host, hosts.keys.toString()))
        }
        return hosts[host]
    }

    override fun getTestStepClass(): Class<UnixShellTestStep> {
        return UnixShellTestStep::class.java
    }

    class Builder : BaseTestStepProcessor.Builder<UnixShellTestStep>() {

        private val hosts = hashMapOf<String, DefaultSshClient>()
        private val unixShellConfigReader = UnixShellConfigReader()

        fun withHost(unixShellHostDetails: UnixShellHostDetails) = apply {
            hosts[unixShellHostDetails.hostName] = DefaultSshClient.Builder()
                    .withHost(unixShellHostDetails.hostAddress)
                    .withPort(unixShellHostDetails.port)
                    .withUser(unixShellHostDetails.userName)
                    .withPassword(unixShellHostDetails.password)
                    .withPassword(unixShellHostDetails.sshKeyPath)
                    .build()
        }

        fun withHost(config: Config) = apply {
            unixShellConfigReader.read(config).forEach { withHost(it) }
        }

        override fun build(): TestStepProcessor<UnixShellTestStep> {
            return UnixShellTestStepProcessor(testScenarioState!!, validator!!, testStepResultConverter, hosts)
        }
    }
}