package org.skellig.teststep.processor.db.model

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.ValidationDetails

open class DatabaseTestStep protected constructor(id: String?,
                                                  name: String,
                                                  execution: TestStepExecutionType?,
                                                  timeout: Int,
                                                  delay: Int,
                                                  variables: Map<String, Any?>?,
                                                  testData: Any?,
                                                  validationDetails: ValidationDetails?,
                                                  val servers: Collection<String>,
                                                  val command: String?,
                                                  val table: String?,
                                                  val query: String?)
    : DefaultTestStep(id, name, execution, timeout, delay, variables, testData, validationDetails) {

    class Builder : DefaultTestStep.Builder<DatabaseTestStep>() {

        private var servers: Collection<String> = emptyList()
        private var command: String? = null
        private var table: String? = null
        private var query: String? = null

        fun withServers(servers: Collection<String>) = apply {
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

        override fun build(): DatabaseTestStep {
            return DatabaseTestStep(id, name!!, execution, timeout,
                    delay, variables, testData,
                    validationDetails, servers, command, table, query)
        }
    }
}