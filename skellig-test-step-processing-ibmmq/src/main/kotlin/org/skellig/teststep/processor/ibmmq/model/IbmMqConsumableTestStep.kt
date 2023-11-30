package org.skellig.teststep.processor.ibmmq.model

import org.skellig.teststep.processing.exception.TestStepCreationException
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.validation.ValidationNode

open class IbmMqConsumableTestStep protected constructor(id: String?,
                                                         name: String?,
                                                         timeout: Int,
                                                         variables: Map<String, Any?>?,
                                                         testData: Any?,
                                                         validationDetails: ValidationNode?,
                                                         val consumeFrom: List<String>,
                                                         val respondTo: List<String>?)
    : DefaultTestStep(id, name!!, TestStepExecutionType.ASYNC, timeout, 0, 0, variables,
                      testData, validationDetails) {


    class Builder : DefaultTestStep.Builder<IbmMqConsumableTestStep>() {

        private var consumeFrom: List<String>? = null
        private var respondTo: List<String>? = null

        fun consumeFrom(consumeFrom: List<String>?) = apply {
            this.consumeFrom = consumeFrom
        }

        fun respondTo(respondTo: List<String>?) = apply {
            this.respondTo = respondTo
        }

        override fun build(): IbmMqConsumableTestStep {
            val consumeChannelsSize = (consumeFrom ?: throw TestStepCreationException(
                "consumeFrom is mandatory for IbmMqConsumableTestStep")).size
            respondTo?.let {
                if (consumeChannelsSize != it.size)
                    throw TestStepCreationException("consumeFrom and respondTo must have the same size")
            }
            return IbmMqConsumableTestStep(id, name!!, timeout, variables, testData,
                                           validationDetails, consumeFrom!!, respondTo)
        }
    }
}