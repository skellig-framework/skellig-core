package org.skellig.teststep.processor.cassandra.model

import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.ValidationDetails
import org.skellig.teststep.processor.db.model.DatabaseTestStep

open class CassandraTestStep protected constructor(id: String?,
                                                   name: String,
                                                   execution: TestStepExecutionType?,
                                                   timeout: Int,
                                                   delay: Int,
                                                   attempts: Int,
                                                   variables: Map<String, Any?>?,
                                                   testData: Any?,
                                                   validationDetails: ValidationDetails?,
                                                   servers: Collection<String>?,
                                                   command: String?,
                                                   table: String?,
                                                   query: String?)
    : DatabaseTestStep(id, name, execution, timeout, delay, attempts, variables, testData, validationDetails,
        servers, command, table, query) {

    class Builder : DatabaseTestStep.Builder<CassandraTestStep>() {
        override fun build(): CassandraTestStep {
            return CassandraTestStep(id, name!!, execution, timeout,
                    delay, attempts, variables, testData,
                    validationDetails, servers, command, table, query)
        }
    }
}