package org.skellig.teststep.processor.rmq.model

import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.validation.ValidationNode

open class RmqTestStep protected constructor(id: String?,
                                             name: String?,
                                             execution: TestStepExecutionType?,
                                             timeout: Int,
                                             delay: Int,
                                             attempts: Int,
                                             variables: Map<String, Any?>?,
                                             testData: Any?,
                                             validationDetails: ValidationNode?,
                                             val sendTo: Set<String>?,
                                             val receiveFrom: Set<String>?,
                                             val respondTo: Set<String>?,
                                             routingKey: String?,
                                             properties: Map<String, Any?>?)
    : BaseRmqTestStep(id, name!!, execution, timeout, delay, attempts,
                      variables, testData, validationDetails, routingKey, properties) {


    class Builder : BaseRmqTestStep.Builder<RmqTestStep>() {

        private var sendTo: Set<String>? = null
        private var receiveFrom: Set<String>? = null
        private var respondTo: Set<String>? = null

        fun sendTo(sendTo: Set<String>?) = apply {
            this.sendTo = sendTo
        }

        fun receiveFrom(receiveFrom: Set<String>?) = apply {
            this.receiveFrom = receiveFrom
        }

        fun respondTo(respondTo: Set<String>?) = apply {
            this.respondTo = respondTo
        }

        override fun build(): RmqTestStep {
            return RmqTestStep(id, name, execution, timeout, delay, attempts, variables, testData, validationDetails,
                    sendTo, receiveFrom, respondTo, routingKey, properties)
        }
    }
}