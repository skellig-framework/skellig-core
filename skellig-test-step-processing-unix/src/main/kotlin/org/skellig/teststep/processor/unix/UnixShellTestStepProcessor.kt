package org.skellig.teststep.processor.unix

import com.typesafe.config.Config
import org.skellig.task.async.AsyncTaskUtils
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

    override fun processTestStep(testStep: UnixShellTestStep): Any? {
        var hostsToUse: Collection<String>? = testStep.hosts
        if (hostsToUse.isNullOrEmpty()) {
            if (hosts.size > 1) {
                throw TestStepProcessingException("No hosts were provided to run a command." +
                                                          " Registered hosts are: ${hosts.keys}")
            } else {
                hostsToUse = hosts.keys
            }
        }


        val tasks = hostsToUse
            .map {
                it to {
                    val sshClient = getSshClient(it)
                    sshClient.runShellCommand(testStep.getCommand(), testStep.timeout)
                }
            }
            .toMap()
        val results = AsyncTaskUtils.runTasksAsyncAndWait(
            tasks,
            { isValid(testStep, it) },
            testStep.delay,
            testStep.attempts,
            testStep.timeout
        )
        return if (isResultForSingleService(results, testStep)) results.values.first() else results
    }

    private fun getSshClient(host: String): DefaultSshClient {
        return hosts[host]
            ?: throw TestStepProcessingException("No hosts was registered for host name '$host'." +
                                                         " Registered hosts are: ${hosts.keys}")
    }

    private fun isResultForSingleService(results: Map<*, *>, testStep: UnixShellTestStep) =
        // when only one service is registered and test step doesn't have service name then return non-grouped result
        results.size == 1 && hosts.size == 1 && testStep.hosts.isNullOrEmpty()

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