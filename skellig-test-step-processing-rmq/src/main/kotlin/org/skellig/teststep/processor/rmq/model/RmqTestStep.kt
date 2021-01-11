package org.skellig.teststep.processor.rmq.model

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.ValidationDetails

open class RmqTestStep protected constructor(id: String?,
                                             name: String?,
                                             execution: TestStepExecutionType?,
                                             timeout: Int, delay: Int,
                                             variables: Map<String, Any?>?,
                                             testData: Any?,
                                             validationDetails: ValidationDetails?,
                                             val sendTo: String?,
                                             val receiveFrom: String?,
                                             val respondTo: String?,
                                             val routingKey: String?)
    : DefaultTestStep(id, name!!, execution, timeout, delay, variables, testData, validationDetails) {


    class Builder : DefaultTestStep.Builder<RmqTestStep>() {

        private var sendTo: String? = null
        private var receiveFrom: String? = null
        private var respondTo: String? = null
        private var routingKey: String? = null

        fun withSendTo(sendTo: String?) = apply {
            this.sendTo = sendTo
        }

        fun withReceiveFrom(receiveFrom: String?) = apply {
            this.receiveFrom = receiveFrom
        }

        fun withRespondTo(respondTo: String?) = apply {
            this.respondTo = respondTo
        }

        fun withRoutingKey(routingKey: String?) = apply {
            this.routingKey = routingKey
        }

        override fun build(): RmqTestStep {
            return RmqTestStep(id, name, execution, timeout, delay, variables, testData, validationDetails,
                    sendTo, receiveFrom, respondTo, routingKey)
        }
    }
}