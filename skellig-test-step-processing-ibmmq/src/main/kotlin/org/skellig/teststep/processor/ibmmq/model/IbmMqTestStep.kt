package org.skellig.teststep.processor.ibmmq.model

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.ScenarioStateUpdater
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.ValidationNode

open class IbmMqTestStep protected constructor(id: String?,
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
                                               val respondTo: Set<String>?)
    : DefaultTestStep(id, name!!, execution, timeout, delay, attempts, values, testData, validationDetails, scenarioStateUpdaters) {

    override fun toString(): String {
        return super.toString() +
                (sendTo?.let { "sendTo = $sendTo\n" } ?: "") +
                (readFrom?.let { "readFrom = $readFrom\n" } ?: "") +
                (respondTo?.let { "respondTo = $respondTo\n" } ?: "")
    }

    class Builder : DefaultTestStep.Builder<IbmMqTestStep>() {
        private var sendTo: Set<String>? = null
        private var readFrom: Set<String>? = null
        private var respondTo: Set<String>? = null

        fun sendTo(sendTo: Set<String>?) = apply {
            this.sendTo = sendTo
        }

        fun readFrom(receiveFrom: Set<String>?) = apply {
            this.readFrom = receiveFrom
        }

        fun respondTo(respondTo: Set<String>?) = apply {
            this.respondTo = respondTo
        }

        override fun build(): IbmMqTestStep {
            return IbmMqTestStep(id, name, execution, timeout, delay, attempts, values, testData, validationDetails,
                scenarioStateUpdaters, sendTo, readFrom, respondTo)
        }
    }
}