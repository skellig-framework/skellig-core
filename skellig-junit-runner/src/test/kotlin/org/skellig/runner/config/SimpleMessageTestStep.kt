package org.skellig.runner.config

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.validation.ValidationNode

class SimpleMessageTestStep private constructor(id: String?,
                                                name: String?,
                                                execution: TestStepExecutionType?,
                                                timeout: Int,
                                                delay: Int,
                                                attempts: Int,
                                                variables: Map<String, Any?>?,
                                                testData: Any?,
                                                validationDetails: ValidationNode?,
                                                val receiver: String?,
                                                val receiveFrom: String?)
    : DefaultTestStep(id, name!!, execution, timeout, delay, attempts, variables, testData, validationDetails) {

    class Builder : DefaultTestStep.Builder<SimpleMessageTestStep>() {

        private var receiver: String? = null
        private var receiveFrom: String? = null

        fun withReceiver(receiver: String?) = apply {
            this.receiver = receiver
        }

        fun withReceiveFrom(receiveFrom: String?) = apply {
            this.receiveFrom = receiveFrom
        }

        override fun build(): SimpleMessageTestStep {
            return SimpleMessageTestStep(id, name, execution, timeout, delay, attempts, variables, testData,
                    validationDetails, receiver, receiveFrom)
        }
    }
}