package org.skellig.teststep.processor.tcp.model

import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.ValidationDetails

open class TcpTestStep protected constructor(id: String?,
                                             name: String,
                                             execution: TestStepExecutionType?,
                                             timeout: Int,
                                             delay: Int,
                                             variables: Map<String, Any?>?,
                                             testData: Any?,
                                             validationDetails: ValidationDetails?,
                                             val sendTo: String?,
                                             val receiveFrom: String?,
                                             val respondTo: String?,
                                             val readBufferSize: Int)
    : TestStep(id, name, execution, timeout, delay, variables, testData, validationDetails) {

    class Builder : TestStep.Builder() {

        private var sendTo: String? = null
        private var receiveFrom: String? = null
        private var respondTo: String? = null
        private var readBufferSize = 1024 * 1024

        fun withReceiveFrom(receiveFrom: String?) = apply {
            this.receiveFrom = receiveFrom
        }

        fun withRespondTo(respondTo: String?) = apply {
            this.respondTo = respondTo
        }

        fun withSendTo(sendTo: String?) = apply {
            this.sendTo = sendTo
        }

        fun withReadBufferSize(readBufferSize: Int) = apply {
            this.readBufferSize = readBufferSize
        }

        override fun build(): TcpTestStep {
            return TcpTestStep(id, name!!, execution, timeout, delay, variables, testData, validationDetails,
                    sendTo, receiveFrom, respondTo, readBufferSize)
        }
    }
}