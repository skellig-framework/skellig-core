package org.skellig.teststep.processor.db

import com.typesafe.config.Config
import org.skellig.task.async.AsyncTaskUtils.Companion.runTasksAsyncAndWait
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.TestStepResultValidator
import org.skellig.teststep.processor.db.model.DatabaseDetails
import org.skellig.teststep.processor.db.model.DatabaseRequest
import org.skellig.teststep.processor.db.model.DatabaseTestStep

abstract class DatabaseTestStepProcessor<T : DatabaseRequestExecutor, TS : DatabaseTestStep>
(private val dbServers: Map<String, T>,
 testScenarioState: TestScenarioState,
 validator: TestStepResultValidator)
    : BaseTestStepProcessor<TS>(testScenarioState, validator) {

    override fun processTestStep(testStep: TS): Any? {
        var services: Collection<String>? = testStep.servers
        if (services.isNullOrEmpty()) {
            if (dbServers.size > 1) {
                throw TestStepProcessingException("No DB servers were provided to run a query." +
                        " Registered servers are: " + dbServers.keys.toString())
            } else {
                services = dbServers.keys
            }
        }

        val tasks = services
                .map { it to { getDatabaseServer(it).execute(getDatabaseRequest(testStep)) } }
                .toMap()
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
        return dbServers[serverName] ?: error("No database was registered for server name '$serverName'." +
                " Registered servers are: ${dbServers.keys}")
    }

    override fun close() {
        dbServers.values.forEach { it.close() }
    }

    abstract class Builder<D : DatabaseDetails, TS: DatabaseTestStep, RE : DatabaseRequestExecutor>
        : BaseTestStepProcessor.Builder<TS>() {

        protected var dbServers = mutableMapOf<String, RE>()

        fun withDbServer(databaseDetails: D) = apply {
            dbServers[databaseDetails.serverName] = createRequestExecutor(databaseDetails)
        }

        abstract fun withDbServers(config: Config): Builder<D, TS, RE>?

        protected abstract fun createRequestExecutor(databaseDetails: D): RE
    }

}