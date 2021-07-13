package org.skellig.teststep.processor.tcp.model

import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.ValidationDetails

open class TcpTestStep protected constructor(id: String?,
                                             name: String,
                                             execution: TestStepExecutionType?,
                                             timeout: Int,
                                             delay: Int,
                                             attempts: Int,
                                             variables: Map<String, Any?>?,
                                             testData: Any?,
                                             validationDetails: ValidationDetails?,
                                             val sendTo: Set<String>?,
                                             val readFrom: Set<String>?,
                                             val respondTo: Set<String>?,
                                             readBufferSize: Int)
    : BaseTcpTestStep(id, name, execution, timeout, delay, attempts, variables, testData, validationDetails, readBufferSize) {

    class Builder : BaseTcpTestStep.Builder<TcpTestStep>() {

        private var sendTo: Set<String>? = null
        private var readFrom: Set<String>? = null
        private var respondTo: Set<String>? = null

        fun sendTo(sendTo: Set<String>?) = apply {
            this.sendTo = sendTo
        }

        fun readFrom(readFrom: Set<String>?) = apply {
            this.readFrom = readFrom
        }

        fun respondTo(respondTo: Set<String>?) = apply {
            this.respondTo = respondTo
        }

        override fun build(): TcpTestStep {
            return TcpTestStep(id, name!!, execution, timeout, delay, attempts, variables, testData, validationDetails,
                               sendTo, readFrom, respondTo, readBufferSize)
        }
    }
}