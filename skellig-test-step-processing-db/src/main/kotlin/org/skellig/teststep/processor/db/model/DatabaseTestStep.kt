package org.skellig.teststep.processor.db.model

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.ValidationDetails

open class DatabaseTestStep protected constructor(id: String?,
                                                  name: String,
                                                  execution: TestStepExecutionType?,
                                                  timeout: Int,
                                                  delay: Int,
                                                  attempts: Int,
                                                  variables: Map<String, Any?>?,
                                                  testData: Any?,
                                                  validationDetails: ValidationDetails?,
                                                  val servers: Collection<String>?,
                                                  val command: String?,
                                                  val table: String?,
                                                  val query: String?)
    : DefaultTestStep(id, name, execution, timeout, delay, attempts, variables, testData, validationDetails) {

    abstract class Builder<T : DatabaseTestStep> : DefaultTestStep.Builder<T>() {

        protected var servers: Collection<String>? = emptyList()
        protected var command: String? = null
        protected var table: String? = null
        protected var query: String? = null

        fun withServers(servers: Collection<String>?) = apply {
            this.servers = servers
        }

        fun withCommand(command: String?) = apply {
            this.command = command
        }

        fun withTable(table: String?) = apply {
            this.table = table
        }

        fun withQuery(query: String?) = apply {
            this.query = query
        }
    }
}