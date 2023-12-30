package org.skellig.teststep.processor.unix.model

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.ScenarioStateUpdater
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.ValidationNode

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

        fun withHosts(hosts: Collection<String>?) = apply {
            this.hosts = hosts
        }

        fun withCommand(command: String?) = apply {
            this.command = command
        }

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