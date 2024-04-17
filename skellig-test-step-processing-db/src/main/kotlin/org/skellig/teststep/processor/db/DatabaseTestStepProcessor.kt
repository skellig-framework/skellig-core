package org.skellig.teststep.processor.db

import com.typesafe.config.Config
import org.skellig.task.async.AsyncTaskUtils.Companion.runTasksAsyncAndWait
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.util.debug
import org.skellig.teststep.processing.util.info
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processor.db.model.DatabaseDetails
import org.skellig.teststep.processor.db.model.DatabaseRequest
import org.skellig.teststep.processor.db.model.DatabaseTestStep


/**
 * Abstract class representing a processor for executing database test steps.
 *
 * @param T the type of the [DatabaseRequestExecutor] to be used for executing database requests.
 * @param TS the type of the [DatabaseTestStep] to be processed.
 * @property dbServers a map of database servers with their corresponding [DatabaseRequestExecutor]s.
 * @property testScenarioState the [TestScenarioState] object that holds the state of the test scenario.
 */
abstract class DatabaseTestStepProcessor<T : DatabaseRequestExecutor, TS : DatabaseTestStep>
    ( private val dbServers: Map<String, T>,
    testScenarioState: TestScenarioState
) : BaseTestStepProcessor<TS>(testScenarioState) {

    private val log = logger<DatabaseTestStepProcessor<T, TS>>()

    override fun processTestStep(testStep: TS): Any? {
        var servers: Collection<String>? = testStep.servers
        if (servers.isNullOrEmpty()) {
            if (dbServers.size > 1) {
                throw TestStepProcessingException(
                    "No DB servers were provided to run a query." +
                            " Registered servers are: " + dbServers.keys.toString()
                )
            } else {
                servers = dbServers.keys
            }
        }
        log.info(testStep, "Start to run DB query of test step '${testStep.name}' in $servers servers")

        val tasks = servers.associateWith { { getDatabaseServer(it).execute(getDatabaseRequest(testStep)) } }
        val results = runTasksAsyncAndWait(tasks, { isValid(testStep, it) }, testStep.delay, testStep.attempts, testStep.timeout)
        return if (isResultForSingleDbServer(results, testStep)) results.values.first() else results
    }

    private fun getDatabaseRequest(testStep: TS): DatabaseRequest {
        return if (testStep.query != null) DatabaseRequest(testStep.query, testStep.testData as List<*>?)
        else DatabaseRequest(testStep.command, testStep.table, testStep.testData as Map<String, Any?>?)
    }

    private fun isResultForSingleDbServer(results: Map<*, *>, testStep: DatabaseTestStep) =
        // when only one db server is registered and test step doesn't have db server names then return non-grouped result
        results.size == 1 && dbServers.size == 1 && testStep.servers.isNullOrEmpty()

    private fun getDatabaseServer(serverName: String): DatabaseRequestExecutor {
        return dbServers[serverName] ?: error(
            "No database was registered for server name '$serverName'." +
                    " Registered servers are: ${dbServers.keys}"
        )
    }

    override fun close() {
        dbServers.values.forEach { it.close() }
    }

    abstract class Builder<D : DatabaseDetails, TS : DatabaseTestStep, RE : DatabaseRequestExecutor>
        : BaseTestStepProcessor.Builder<TS>() {

        private val log = logger<Builder<D, TS, RE>>()

        protected var dbServers = mutableMapOf<String, RE>()

        fun withDbServer(databaseDetails: D) = apply {
            log.debug { "Register database server '${databaseDetails.serverName}' with details: $databaseDetails" }
            dbServers[databaseDetails.serverName] = createRequestExecutor(databaseDetails)
        }

        abstract fun withDbServers(config: Config): Builder<D, TS, RE>?

        protected abstract fun createRequestExecutor(databaseDetails: D): RE
    }

}