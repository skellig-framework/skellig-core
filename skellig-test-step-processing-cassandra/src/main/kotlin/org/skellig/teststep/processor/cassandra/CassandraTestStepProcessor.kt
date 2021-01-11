package org.skellig.teststep.processor.cassandra

import com.typesafe.config.Config
import org.skellig.teststep.processing.converter.TestStepResultConverter
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.TestStepResultValidator
import org.skellig.teststep.processor.cassandra.model.CassandraDetails
import org.skellig.teststep.processor.db.DatabaseTestStepProcessor
import org.skellig.teststep.processor.db.model.DatabaseTestStep

open class CassandraTestStepProcessor protected constructor(dbServers: Map<String, CassandraRequestExecutor>,
                                                            testScenarioState: TestScenarioState,
                                                            validator: TestStepResultValidator,
                                                            testStepResultConverter: TestStepResultConverter?)
    : DatabaseTestStepProcessor<CassandraRequestExecutor>(dbServers, testScenarioState, validator, testStepResultConverter) {

    class Builder : DatabaseTestStepProcessor.Builder<CassandraDetails, CassandraRequestExecutor>() {

        private val cassandraDetailsConfigReader = CassandraDetailsConfigReader()

        override fun withDbServers(config: Config) = apply {
            cassandraDetailsConfigReader.read(config).forEach { withDbServer(it) }
        }

        protected override fun createRequestExecutor(databaseDetails: CassandraDetails): CassandraRequestExecutor {
            return CassandraRequestExecutor(databaseDetails)
        }

        override fun build(): TestStepProcessor<DatabaseTestStep> {
            return CassandraTestStepProcessor(dbServers, testScenarioState!!, validator!!, testStepResultConverter)
        }
    }
}