package org.skellig.teststep.processor.jdbc.model

import org.skellig.teststep.processing.model.ScenarioStateUpdater
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.ValidationNode
import org.skellig.teststep.processor.db.model.DatabaseTestStep

open class JdbcTestStep protected constructor(id: String?,
                                              name: String,
                                              execution: TestStepExecutionType?,
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