package org.skellig.runner.config

import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.ValidationDetails

class SimpleMessageTestStep private constructor(id: String?,
                                                name: String?,
                                                execution: TestStepExecutionType?,
                                                timeout: Int,
                                                delay: Int,
                                                variables: Map<String, Any?>?,
                                                testData: Any?,
                                                validationDetails: ValidationDetails?,
                                                val receiver: String?,
                                                val receiveFrom: String?)
    : TestStep(id, name!!, execution, timeout, delay, variables, testData, validationDetails) {

    class Builder : TestStep.Builder() {

        private var receiver: String? = null
        private var receiveFrom: String? = null

        fun withReceiver(receiver: String?) = apply {
            this.receiver = receiver
        }

        fun withReceiveFrom(receiveFrom: String?) = apply {
            this.receiveFrom = receiveFrom
        }

        override fun build(): TestStep {
            return SimpleMessageTestStep(id, name, execution, timeout, delay, variables, testData,
                    validationDetails, receiver, receiveFrom)
        }
    }
}