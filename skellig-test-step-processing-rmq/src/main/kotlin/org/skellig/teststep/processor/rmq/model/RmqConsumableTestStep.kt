package org.skellig.teststep.processor.rmq.model

import org.skellig.teststep.processing.exception.TestStepCreationException
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.ValidationDetails

open class RmqConsumableTestStep protected constructor(id: String?,
                                                       name: String?,
                                                       timeout: Int,
                                                       variables: Map<String, Any?>?,
                                                       testData: Any?,
                                                       validationDetails: ValidationDetails?,
                                                       val consumeFrom: List<String>,
                                                       val respondTo: List<String>?,
                                                       routingKey: String?,
                                                       properties: Map<String, Any?>?)
    : BaseRmqTestStep(id, name!!, TestStepExecutionType.ASYNC, timeout, 0, 0, variables,
                      testData, validationDetails, routingKey, properties) {


    class Builder : BaseRmqTestStep.Builder<RmqConsumableTestStep>() {

        private var consumeFrom: List<String>? = null
        private var respondTo: List<String>? = null

        fun consumeFrom(consumeFrom: List<String>?) = apply {
            this.consumeFrom = consumeFrom
        }

        fun respondTo(respondTo: List<String>?) = apply {
            this.respondTo = respondTo
        }

        override fun build(): RmqConsumableTestStep {
            val consumeChannelsSize = (consumeFrom ?: throw TestStepCreationException(
                "consumeFrom is mandatory for RmqConsumableTestStep")).size
            respondTo?.let {
                if (consumeChannelsSize != it.size)
                    throw TestStepCreationException("consumeFrom and respondTo must have the same size")
            }

            return RmqConsumableTestStep(id,
                                         name,
                                         timeout,
                                         variables,
                                         testData,
                                         validationDetails,
                                         consumeFrom!!,
                                         respondTo,
                                         routingKey,
                                         properties)
        }
    }
}