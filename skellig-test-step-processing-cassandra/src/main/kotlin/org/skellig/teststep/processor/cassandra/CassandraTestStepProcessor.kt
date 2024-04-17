package org.skellig.teststep.processor.cassandra

import com.typesafe.config.Config
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processor.cassandra.model.CassandraDetails
import org.skellig.teststep.processor.cassandra.model.CassandraTestStep
import org.skellig.teststep.processor.db.DatabaseTestStepProcessor

/**
 * CassandraTestStepProcessor is a class that processes Cassandra test steps for executing database requests.
 *
 * @property dbServers a map of database servers with their corresponding [CassandraRequestExecutor], where key is a server name from [servers][CassandraTestStep.servers]
 * @property testScenarioState the [TestScenarioState] object that holds the state of the test scenario.
 */
open class CassandraTestStepProcessor protected constructor(dbServers: Map<String, CassandraRequestExecutor>,
                                                            testScenarioState: TestScenarioState)
    : DatabaseTestStepProcessor<CassandraRequestExecutor, CassandraTestStep>(dbServers, testScenarioState) {

    override fun getTestStepClass(): Class<CassandraTestStep> = CassandraTestStep::class.java

    class Builder : DatabaseTestStepProcessor.Builder<CassandraDetails,CassandraTestStep, CassandraRequestExecutor>() {

        private val cassandraDetailsConfigReader = CassandraDetailsConfigReader()

        override fun withDbServers(config: Config) = apply {
            cassandraDetailsConfigReader.read(config).forEach { withDbServer(it) }
        }

        override fun createRequestExecutor(databaseDetails: CassandraDetails): CassandraRequestExecutor {
            return CassandraRequestExecutor(databaseDetails)
        }

        override fun build(): TestStepProcessor<CassandraTestStep> {
            return CassandraTestStepProcessor(dbServers, testScenarioState!!)
        }
    }
}