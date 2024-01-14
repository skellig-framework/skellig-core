package org.skellig.teststep.processor.tcp.model

import org.skellig.teststep.processing.exception.TestStepCreationException
import org.skellig.teststep.processing.model.ScenarioStateUpdater
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.ValidationNode

open class TcpConsumableTestStep protected constructor(id: String?,
                                                       name: String?,
                                                       timeout: Int,
                                                       values: Map<String, Any?>?,
                                                       testData: Any?,
                                                       validationDetails: ValidationNode?,
                                                       scenarioStateUpdaters: List<ScenarioStateUpdater>?,
                                                       val consumeFrom: List<String>,
                                                       val respondTo: List<String>?,
                                                       readBufferSize: Int)
    : BaseTcpTestStep(id, name!!, TestStepExecutionType.ASYNC, timeout, 0, 0, values,
                      testData, validationDetails, scenarioStateUpdaters, readBufferSize) {

    override fun toString(): String {
        return super.toString() + "consumeFrom = $consumeFrom\n" +
                (respondTo?.let { "respondTo = $respondTo\n" } ?: "")
    }

    class Builder : BaseTcpTestStep.Builder<TcpConsumableTestStep>() {

        private var consumeFrom: List<String>? = null
        private var respondTo: List<String>? = null

        fun consumeFrom(consumeFrom: List<String>?) = apply {
            this.consumeFrom = consumeFrom
        }

        fun respondTo(respondTo: List<String>?) = apply {
            this.respondTo = respondTo
        }

        override fun build(): TcpConsumableTestStep {
            val consumeChannelsSize = (consumeFrom ?: throw TestStepCreationException(
                "consumeFrom is mandatory for TcpConsumableTestStep")).size
            respondTo?.let {
                if (consumeChannelsSize != it.size)
                    throw TestStepCreationException("consumeFrom and respondTo must have the same size")
            }
            return TcpConsumableTestStep(id, name!!, timeout, values, testData,
                                         validationDetails, scenarioStateUpdaters, consumeFrom!!, respondTo, readBufferSize)
        }
    }
}