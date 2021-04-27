package org.skellig.teststep.processor.tcp.model

import org.skellig.teststep.processing.model.DefaultTestStep
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
                                             val readBufferSize: Int)
    : DefaultTestStep(id, name, execution, timeout, delay, attempts, variables, testData, validationDetails) {

    class Builder : DefaultTestStep.Builder<TcpTestStep>() {

        private var sendTo: Set<String>? = null
        private var readFrom: Set<String>? = null
        private var respondTo: Set<String>? = null
        private var readBufferSize = 1024 * 1024

        fun sendTo(sendTo: Set<String>?) = apply {
            this.sendTo = sendTo
        }

        fun readFrom(readFrom: Set<String>?) = apply {
            this.readFrom = readFrom
        }

        fun respondTo(respondTo: Set<String>?) = apply {
            this.respondTo = respondTo
        }

        fun readBufferSize(readBufferSize: Int) = apply {
            this.readBufferSize = readBufferSize
        }

        override fun build(): TcpTestStep {
            return TcpTestStep(id, name!!, execution, timeout, delay, attempts, variables, testData, validationDetails,
                    sendTo, readFrom, respondTo, readBufferSize)
        }
    }
}