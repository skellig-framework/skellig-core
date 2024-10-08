package org.skellig.teststep.processor.unix

import com.typesafe.config.Config
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.util.debug
import org.skellig.teststep.processing.util.info
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processor.unix.model.UnixShellHostDetails
import org.skellig.teststep.processor.unix.model.UnixShellTestStep

/**
 * UnixShellTestStepProcessor is a class that processes test steps containing Unix Shell commands.
 * The command runs in parallel on each host defined in [UnixShellTestStep]. After it gets result from all parallel runs,
 * it's validated based on details provided in [UnixShellTestStep.validationDetails] and if not valid, it will retry the
 * same processing with N-[attempts][UnixShellTestStep.attempts] (if more than 0).
 *
 * Each individual run of Unix Shell command per host, can be limited by a [timeout][UnixShellTestStep.timeout].
 *
 * The result of test step processing is a Map where key is a host name from [hosts][UnixShellTestStep.hosts] and value
 * is a response from execution of the command. If only 1 host name is provided in [hosts][UnixShellTestStep.hosts], then
 * the result is String - response from execution of the command.
 *
 * @param testScenarioState The test scenario state.
 * @param hosts The map of host names and their corresponding [DefaultSshClient] instances.
 */
class UnixShellTestStepProcessor(
    testScenarioState: TestScenarioState,
    internal val hosts: Map<String, DefaultSshClient>
) : BaseTestStepProcessor<UnixShellTestStep>(testScenarioState) {

    private val log = logger<UnixShellTestStepProcessor>()

    override fun processTestStep(testStep: UnixShellTestStep): Any? {
        var hostsToUse: Collection<String>? = testStep.hosts
        if (hostsToUse.isNullOrEmpty()) {
            if (hosts.size > 1) {
                throw TestStepProcessingException(
                    "No hosts were provided to run a command." +
                            " Registered hosts are: ${hosts.keys}"
                )
            } else {
                hostsToUse = hosts.keys
            }
        }

        log.info(testStep, "Start to run remote Unix Shell commands of test step '${testStep.name}' in $hosts hosts")
        val tasks = hostsToUse.associateWith {
            {
                val sshClient = getSshClient(it)
                log.debug(testStep) { "Run remote Unix Shell command: '${testStep.getCommand()}' on host '$it'" }
                val response = sshClient.runShellCommand(testStep.getCommand(), testStep.timeout)
                log.debug(testStep) { "Received response from Unix Shell command: '${testStep.getCommand()}' from host '$it': $response" }
                response
            }
        }
        val results = runTasksAsyncAndWait(tasks, testStep)
        return if (isResultForSingleService(results, testStep)) results.values.first() else results
    }

    private fun getSshClient(host: String): DefaultSshClient {
        return hosts[host]
            ?: throw TestStepProcessingException(
                "No hosts was registered for host name '$host'." +
                        " Registered hosts are: ${hosts.keys}"
            )
    }

    private fun isResultForSingleService(results: Map<*, *>, testStep: UnixShellTestStep) =
        // when only one service is registered and test step doesn't have service name then return non-grouped result
        results.size == 1 && hosts.size == 1 && testStep.hosts.isNullOrEmpty()

    override fun getTestStepClass(): Class<UnixShellTestStep> {
        return UnixShellTestStep::class.java
    }

    /**
     * Closes the Unix Shell Test Step Processor and all SSH connections with hosts.
     *
     * This method iterates over the values of the 'hosts' map and calls the 'close()' method on each SSH connection.
     *
     * @see UnixShellTestStepProcessor
     */
    override fun close() {
        log.info("Close Unix Shell Test Step Processor and all SSH connections with hosts")
        hosts.values.forEach { it.close() }
    }

    class Builder : BaseTestStepProcessor.Builder<UnixShellTestStep>() {

        private val hosts = hashMapOf<String, DefaultSshClient>()
        private val unixShellConfigReader = UnixShellConfigReader()

        fun withHosts(unixShellHostDetails: UnixShellHostDetails) = apply {
            hosts[unixShellHostDetails.hostName] = DefaultSshClient.Builder()
                .withHost(unixShellHostDetails.hostAddress)
                .withPort(unixShellHostDetails.port)
                .withUser(unixShellHostDetails.userName)
                .withPassword(unixShellHostDetails.password)
                .withPrivateSshKeyPath(unixShellHostDetails.sshKeyPath)
                .withFingerprint(unixShellHostDetails.fingerprint)
                .build()
        }

        /**
         * Applies the provided configuration to the current builder instance by reading the UnixShellHostDetails from the given Skellig Config
         * and register a connection for each host.
         *
         * @param config The Skellig Config containing the configuration of Unix Shell Processor (see [UnixShellConfigReader]).
         * @return The modified builder instance.
         */
        fun withHosts(config: Config) = apply {
            unixShellConfigReader.read(config).forEach { withHosts(it) }
        }

        override fun build(): TestStepProcessor<UnixShellTestStep> {
            return UnixShellTestStepProcessor(testScenarioState!!, hosts)
        }
    }
}