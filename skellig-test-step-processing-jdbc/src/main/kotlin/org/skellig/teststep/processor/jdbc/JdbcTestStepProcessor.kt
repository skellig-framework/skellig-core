package org.skellig.teststep.processor.jdbc

import com.typesafe.config.Config
import org.skellig.teststep.processing.converter.TestStepResultConverter
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.TestStepResultValidator
import org.skellig.teststep.processor.db.DatabaseTestStepProcessor
import org.skellig.teststep.processor.db.model.DatabaseTestStep
import org.skellig.teststep.processor.jdbc.model.JdbcDetails

open class JdbcTestStepProcessor protected constructor(dbServers: Map<String, JdbcRequestExecutor>,
                                                       testScenarioState: TestScenarioState,
                                                       validator: TestStepResultValidator,
                                                       testStepResultConverter: TestStepResultConverter?)
    : DatabaseTestStepProcessor<JdbcRequestExecutor>(dbServers, testScenarioState, validator, testStepResultConverter) {

    class Builder : DatabaseTestStepProcessor.Builder<JdbcDetails, JdbcRequestExecutor>() {

        private val jdbcDetailsConfigReader: JdbcDetailsConfigReader = JdbcDetailsConfigReader()

        override fun withDbServers(config: Config) = apply {
            jdbcDetailsConfigReader.read(config).forEach { databaseDetails: JdbcDetails -> withDbServer(databaseDetails) }
        }

        protected override fun createRequestExecutor(databaseDetails: JdbcDetails): JdbcRequestExecutor {
            return JdbcRequestExecutor(databaseDetails)
        }

        override fun build(): TestStepProcessor<DatabaseTestStep> {
            return JdbcTestStepProcessor(dbServers, testScenarioState!!, validator!!, testStepResultConverter)
        }

    }
}