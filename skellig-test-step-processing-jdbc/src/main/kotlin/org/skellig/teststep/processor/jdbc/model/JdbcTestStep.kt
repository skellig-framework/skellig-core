package org.skellig.teststep.processor.jdbc.model

import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.validation.ValidationNode
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
                                              servers: Collection<String>?,
                                              command: String?,
                                              table: String?,
                                              query: String?)
    : DatabaseTestStep(id, name, execution, timeout, delay, attempts, values, testData, validationDetails,
        servers, command, table, query) {

    class Builder : DatabaseTestStep.Builder<JdbcTestStep>() {
        override fun build(): JdbcTestStep {
            return JdbcTestStep(id, name!!, execution, timeout,
                    delay, attempts, values, testData,
                    validationDetails, servers, command, table, query)
        }
    }
}