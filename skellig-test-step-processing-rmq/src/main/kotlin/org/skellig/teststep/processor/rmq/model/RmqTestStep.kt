package org.skellig.teststep.processor.rmq.model

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.ValidationDetails

open class RmqTestStep protected constructor(id: String?,
                                             name: String?,
                                             execution: TestStepExecutionType?,
                                             timeout: Int,
                                             delay: Int,
                                             attempts: Int,
                                             variables: Map<String, Any?>?,
                                             testData: Any?,
                                             validationDetails: ValidationDetails?,
                                             val sendTo: Set<String>?,
                                             val receiveFrom: Set<String>?,
                                             val respondTo: Set<String>?,
                                             val routingKey: String?)
    : DefaultTestStep(id, name!!, execution, timeout, delay, attempts, variables, testData, validationDetails) {


    class Builder : DefaultTestStep.Builder<RmqTestStep>() {

        private var sendTo: Set<String>? = null
        private var receiveFrom: Set<String>? = null
        private var respondTo: Set<String>? = null
        private var routingKey: String? = null

        fun sendTo(sendTo: Set<String>?) = apply {
            this.sendTo = sendTo
        }

        fun receiveFrom(receiveFrom: Set<String>?) = apply {
            this.receiveFrom = receiveFrom
        }

        fun respondTo(respondTo: Set<String>?) = apply {
            this.respondTo = respondTo
        }

        fun routingKey(routingKey: String?) = apply {
            this.routingKey = routingKey
        }

        override fun build(): RmqTestStep {
            return RmqTestStep(id, name, execution, timeout, delay, attempts, variables, testData, validationDetails,
                    sendTo, receiveFrom, respondTo, routingKey)
        }
    }
}