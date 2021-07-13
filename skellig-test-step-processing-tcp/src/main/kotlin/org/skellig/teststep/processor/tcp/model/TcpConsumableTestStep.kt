package org.skellig.teststep.processor.tcp.model

import org.skellig.teststep.processing.exception.TestStepCreationException
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.ValidationDetails

open class TcpConsumableTestStep protected constructor(id: String?,
                                                       name: String?,
                                                       timeout: Int,
                                                       variables: Map<String, Any?>?,
                                                       testData: Any?,
                                                       validationDetails: ValidationDetails?,
                                                       val consumeFrom: List<String>,
                                                       val respondTo: List<String>?,
                                                       readBufferSize: Int)
    : BaseTcpTestStep(id, name!!, TestStepExecutionType.ASYNC, timeout, 0, 0, variables,
                      testData, validationDetails, readBufferSize) {


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
                "consumeFrom is mandatory for RmqConsumableTestStep")).size
            respondTo?.let {
                if (consumeChannelsSize != it.size)
                    throw TestStepCreationException("consumeFrom and respondTo must have the same size")
            }
            return TcpConsumableTestStep(id, name!!, timeout, variables, testData,
                                         validationDetails, consumeFrom!!, respondTo, readBufferSize)
        }
    }
}