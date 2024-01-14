package org.skellig.teststep.processor.rmq.model

import org.skellig.teststep.processing.model.ScenarioStateUpdater
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.ValidationNode

open class RmqTestStep protected constructor(id: String?,
                                             name: String?,
                                             execution: TestStepExecutionType?,
                                             timeout: Int,
                                             delay: Int,
                                             attempts: Int,
                                             values: Map<String, Any?>?,
                                             testData: Any?,
                                             validationDetails: ValidationNode?,
                                             scenarioStateUpdaters: List<ScenarioStateUpdater>?,
                                             val sendTo: Set<String>?,
                                             val readFrom: Set<String>?,
                                             val respondTo: Set<String>?,
                                             routingKey: String?,
                                             properties: Map<String, Any?>?)
    : BaseRmqTestStep(id, name!!, execution, timeout, delay, attempts,
                      values, testData, validationDetails, scenarioStateUpdaters, routingKey, properties) {

    override fun toString(): String {
        return super.toString() +
                (sendTo?.let { "sendTo = $sendTo\n" } ?: "") +
                (readFrom?.let { "readFrom = $readFrom\n" } ?: "") +
                (respondTo?.let { "respondTo = $respondTo\n" } ?: "")
    }

    class Builder : BaseRmqTestStep.Builder<RmqTestStep>() {

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

        override fun build(): RmqTestStep {
            return RmqTestStep(id, name, execution, timeout, delay, attempts, values, testData, validationDetails,
                scenarioStateUpdaters, sendTo, readFrom, respondTo, routingKey, properties)
        }
    }
}