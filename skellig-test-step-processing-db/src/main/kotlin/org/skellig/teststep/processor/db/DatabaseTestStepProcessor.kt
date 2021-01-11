package org.skellig.teststep.processor.db

import com.typesafe.config.Config
import org.skellig.teststep.processing.converter.TestStepResultConverter
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.TestStepResultValidator
import org.skellig.teststep.processor.db.model.DatabaseDetails
import org.skellig.teststep.processor.db.model.DatabaseRequest
import org.skellig.teststep.processor.db.model.DatabaseTestStep

open class DatabaseTestStepProcessor<T : DatabaseRequestExecutor>(private val dbServers: Map<String, T>,
                                                                  testScenarioState: TestScenarioState,
                                                                  validator: TestStepResultValidator,
                                                                  testStepResultConverter: TestStepResultConverter?)
    : BaseTestStepProcessor<DatabaseTestStep>(testScenarioState, validator, testStepResultConverter) {

    override fun processTestStep(testStep: DatabaseTestStep): Any? {
        if (testStep.servers.isEmpty()) {
            throw TestStepProcessingException("No DB servers were provided to run a query." +
                    " Registered servers are: " + dbServers.keys.toString())
        }
        val result = mutableMapOf<String, Any?>()
        testStep.servers
                .forEach { serverName: String ->
                    val response = getDatabaseServer(serverName)!!.execute(getDatabaseRequest(testStep))
                    result[serverName] = response
                }
        return result
    }

    private fun getDatabaseRequest(testStep: DatabaseTestStep): DatabaseRequest {
        return if (testStep.query != null) {
            DatabaseRequest(testStep.query)
        } else {
            DatabaseRequest(testStep.command, testStep.table, testStep.testData as Map<String, Any?>?)
        }
    }

    private fun getDatabaseServer(serverName: String): DatabaseRequestExecutor? {
        if (!dbServers.containsKey(serverName)) {
            throw TestStepProcessingException(String.format("No database was registered for server name '%s'." +
                    " Registered servers are: %s", serverName, dbServers.keys.toString()))
        }
        return dbServers[serverName]
    }

    override fun getTestStepClass(): Class<DatabaseTestStep> {
        return DatabaseTestStep::class.java
    }

    override fun close() {
        dbServers.values.forEach { it.close() }
    }

    abstract class Builder<D : DatabaseDetails, RE : DatabaseRequestExecutor> : BaseTestStepProcessor.Builder<DatabaseTestStep>() {

        protected var dbServers = mutableMapOf<String, RE>()

        fun withDbServer(databaseDetails: D) = apply {
            dbServers[databaseDetails.serverName] = createRequestExecutor(databaseDetails)
        }

        abstract fun withDbServers(config: Config): Builder<D, RE>?

        protected abstract fun createRequestExecutor(databaseDetails: D): RE

        abstract override fun build(): TestStepProcessor<DatabaseTestStep>

    }

}