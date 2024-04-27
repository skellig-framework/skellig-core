package org.skellig.teststep.processor.db.model

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.ScenarioStateUpdater
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.ValidationNode

/**
 * Represents a test step that interacts with a database.
 *
 * @param id a unique id of the test step. If not provided, the name of the test step will be used as the id.
 * @param name the name of the test step.
 * @param execution the execution type of the test step, can be "SYNC" or "ASYNC".
 * @param timeout the maximum processing time for the test step in milliseconds.
 * @param delay the delay time in milliseconds between execution attempts of the test step.
 * @param attempts the maximum number of attempts for running the test step before considering it as failed.
 * @param values a map of values that can be used within the test step. The values can be referenced using the "${}" notation.
 * @param testData the test data for the test step, it can be any type of data representing a message or a request used for processing the test step.
 * @param validationDetails the validation details used to validate the processing result of the test step.
 * @param scenarioStateUpdaters the list of scenario state updaters used to update the test scenario state with new values based on the result of the test step execution.
 * @param servers the collection of database server names to connect to.
 * @param command the database command to execute.
 * @param table the database table to interact with.
 * @param query the database query to execute.
 */
open class DatabaseTestStep protected constructor(id: String?,
                                                  name: String,
                                                  execution: TestStepExecutionType,
                                                  timeout: Int,
                                                  delay: Int,
                                                  attempts: Int,
                                                  values: Map<String, Any?>?,
                                                  testData: Any?,
                                                  validationDetails: ValidationNode?,
                                                  scenarioStateUpdaters: List<ScenarioStateUpdater>?,
                                                  val servers: Collection<String>?,
                                                  val command: String?,
                                                  val table: String?,
                                                  val query: String?)
    : DefaultTestStep(id, name, execution, timeout, delay, attempts, values, testData, validationDetails, scenarioStateUpdaters) {

    override fun toString(): String {
        return super.toString() + (query?.let { "servers = $servers\nquery=$query" }
            ?: "servers = $servers\ncommand = $command\ntable = $table\n")
    }

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