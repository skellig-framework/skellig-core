package org.skellig.teststep.processor.jdbc

import com.typesafe.config.Config
import org.skellig.teststep.processing.converter.TestStepResultConverter
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.TestStepResultValidator
import org.skellig.teststep.processor.db.DatabaseTestStepProcessor
import org.skellig.teststep.processor.jdbc.model.JdbcDetails
import org.skellig.teststep.processor.jdbc.model.JdbcTestStep

open class JdbcTestStepProcessor protected constructor(dbServers: Map<String, JdbcRequestExecutor>,
                                                       testScenarioState: TestScenarioState,
                                                       validator: TestStepResultValidator,
                                                       testStepResultConverter: TestStepResultConverter?)
    : DatabaseTestStepProcessor<JdbcRequestExecutor, JdbcTestStep>(dbServers, testScenarioState, validator, testStepResultConverter) {

    override fun getTestStepClass(): Class<*> = JdbcTestStep::class.java

    class Builder : DatabaseTestStepProcessor.Builder<JdbcDetails, JdbcTestStep, JdbcRequestExecutor>() {

        private val jdbcDetailsConfigReader: JdbcDetailsConfigReader = JdbcDetailsConfigReader()

        override fun withDbServers(config: Config) = apply {
            jdbcDetailsConfigReader.read(config).forEach { databaseDetails: JdbcDetails -> withDbServer(databaseDetails) }
        }

        override fun createRequestExecutor(databaseDetails: JdbcDetails): JdbcRequestExecutor {
            return JdbcRequestExecutor(databaseDetails)
        }

        override fun build(): TestStepProcessor<JdbcTestStep> {
            return JdbcTestStepProcessor(dbServers, testScenarioState!!, validator!!, testStepResultConverter)
        }
    }
}