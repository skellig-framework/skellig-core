package org.skellig.teststep.processor.jdbc.model

import org.skellig.teststep.processing.model.ScenarioStateUpdater
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.ValidationNode
import org.skellig.teststep.processor.db.model.DatabaseTestStep

/**
 * Represents a test step that interacts with a database using JDBC.
 * To execute a DB request, the test step must have one of these properties defined:
 * 1) [command], [table]
 * 2) [query], [testData]
 *
 *
 * @param id a unique ID of the test step. If not provided, the name of the test step will be used as the ID.
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
 * @param query SQL query to execute in database.
 */
open class JdbcTestStep protected constructor(id: String?,
                                              name: String,
                                              execution: TestStepExecutionType,
                                              timeout: Int,
                                              delay: Int,
                                              attempts: Int,
                                              values: Map<String, Any?>?,
                                              testData: Any?,
                                              validationDetails: ValidationNode?,
                                              scenarioStateUpdaters: List<ScenarioStateUpdater>?,
                                              servers: Collection<String>?,
                                              command: String?,
                                              table: String?,
                                              query: String?)
    : DatabaseTestStep(id, name, execution, timeout, delay, attempts, values, testData, validationDetails, scenarioStateUpdaters,
        servers, command, table, query) {

    class Builder : DatabaseTestStep.Builder<JdbcTestStep>() {
        override fun build(): JdbcTestStep {
            return JdbcTestStep(id, name!!, execution, timeout,
                    delay, attempts, values, testData,
                    validationDetails, scenarioStateUpdaters, servers, command, table, query)
        }
    }
}