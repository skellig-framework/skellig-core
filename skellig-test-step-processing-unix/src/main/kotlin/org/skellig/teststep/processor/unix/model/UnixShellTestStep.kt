package org.skellig.teststep.processor.unix.model

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.ScenarioStateUpdater
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.ValidationNode

/**
 * The `UnixShellTestStep` class represents a test step that has all details required for [UnixShellTestStepProcessor][org.skellig.teststep.processor.unix.UnixShellTestStepProcessor]
 * to execute a command in a Unix shell environment.
 *
 * @param id a unique id of the test step. It is usually used to store its data or a result in the scenario state, or
 * used to extend other test steps.
 * The result of test step execution is stored in [TestScenarioState][org.skellig.teststep.processing.state.TestScenarioState]
 * in the key "[id]_result". If id is null, then calling [DefaultTestStep.getId] returns [name] of the test step.
 * @param name The name of the test step.
 * @param execution The execution type of the test step. It can be either `SYNC` or `ASYNC`.
 * @param timeout The maximum time in milliseconds allowed for the execution of the test step.
 * @param delay The delay time in milliseconds between execution attempts of the test step.
 * @param attempts The maximum number of attempts for running the test step before it fails.
 * @param values The list of values of the test step. The value of a variable can be referenced within the test step using `${}` notation.
 * @param testData Any type of data representing a message or request used when processing the test step.
 * @param validationDetails A structure used to validate the processing result of the test step or another test step.
 * @param scenarioStateUpdaters A list of `ScenarioStateUpdater` objects used to update the test scenario state.
 * @param hosts A collection of hosts on which the command should be executed.
 * @param command The command to be executed in the Unix shell environment.
 * @param args The arguments to be passed to the command.
 */
open class UnixShellTestStep protected constructor(
    id: String?,
    name: String?,
    execution: TestStepExecutionType?,
    timeout: Int,
    delay: Int,
    attempts: Int,
    values: Map<String, Any?>?,
    testData: Any?,
    validationDetails: ValidationNode?,
    scenarioStateUpdaters: List<ScenarioStateUpdater>?,
    val hosts: Collection<String>?,
    private val command: String,
    private val args: Map<String, String?>?
) : DefaultTestStep(id, name!!, execution, timeout, delay, attempts, values, testData, validationDetails, scenarioStateUpdaters) {

    /**
     * Returns the Unix Shell command.
     *
     * If args is null, it returns the command string directly.
     * Otherwise, it appends the key-value pairs of args to the command string.
     *
     * @return the command string.
     */
    fun getCommand(): String {
        return if (args == null) command else "$command " + args.entries.joinToString(" ") { "-" + it.key + " " + it.value }
    }


    class Builder : DefaultTestStep.Builder<UnixShellTestStep>() {

        companion object {
            private const val DEFAULT_TIMEOUT = 30000
        }

        private var hosts: Collection<String>? = null
        private var command: String? = null
        private var args: Map<String, String>? = null

        init {
            timeout = DEFAULT_TIMEOUT
        }

        /**
         * Sets the collection of hosts for the UnixShellTestStep to identify where to run the command.
         * These are the name of hosts configured in Skellig Config file for the 'unix-shell' processor.
         *
         * @param hosts The collection of hosts.
         * @return The instance of the Builder.
         */
        fun withHosts(hosts: Collection<String>?) = apply {
            this.hosts = hosts
        }

        /**
         * Sets the Unix Shell command for the UnixShellTestStep.
         *
         * @param command The command to be executed.
         * @return The instance of the Builder.
         */
        fun withCommand(command: String?) = apply {
            this.command = command
        }

        /**
         * Sets the arguments for the Unix Shell command.
         *
         * @param args The arguments to be set.
         * @return The instance of the Builder.
         */
        fun withArgs(args: Map<String, String>?) = apply {
            this.args = args
        }

        override fun build(): UnixShellTestStep {
            return UnixShellTestStep(
                id, name, execution, timeout, delay, attempts, values, testData, validationDetails,
                scenarioStateUpdaters, hosts, command ?: error("Unix Shell Command cannot be null"), args
            )
        }
    }
}