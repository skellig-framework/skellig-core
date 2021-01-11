package org.skellig.teststep.processor.unix.model

import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.ValidationDetails

open class UnixShellTestStep protected constructor(id: String?,
                                                   name: String?,
                                                   execution: TestStepExecutionType?,
                                                   timeout: Int,
                                                   delay: Int,
                                                   variables: Map<String, Any?>?,
                                                   testData: Any?,
                                                   validationDetails: ValidationDetails?,
                                                   val hosts: Collection<String>,
                                                   private val command: String,
                                                   private val args: Map<String, String?>?)
    : TestStep(id, name!!, execution, timeout, delay, variables, testData, validationDetails) {

    fun getCommand(): String {
        return if (args == null) command else "$command " + args.entries.joinToString(" ") { "-" + it.key + " " + it.value }
    }

    class Builder : TestStep.Builder() {

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
            return UnixShellTestStep(id, name, execution, timeout, delay, variables, testData, validationDetails,
                    hosts ?: error("Hosts to run Unix Shell Command must not be null"),
                    command ?: error("Unix Shell Command cannot be null"), args)
        }
    }
}