package org.skellig.teststep.processor.rmq.model

import org.skellig.teststep.processing.exception.TestStepCreationException
import org.skellig.teststep.processing.model.ScenarioStateUpdater
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.ValidationNode

open class RmqConsumableTestStep protected constructor(
    id: String?,
    name: String?,
    timeout: Int,
    values: Map<String, Any?>?,
    testData: Any?,
    validationDetails: ValidationNode?,
    scenarioStateUpdaters: List<ScenarioStateUpdater>?,
    val consumeFrom: List<String>,
    val respondTo: List<String>?,
    routingKey: String?,
    properties: Map<String, Any?>?
) : BaseRmqTestStep(
    id, name!!, TestStepExecutionType.ASYNC, timeout, 0, 0, values,
    testData, validationDetails, scenarioStateUpdaters, routingKey, properties
) {

    override fun toString(): String {
        return super.toString() + "consumeFrom = $consumeFrom\n" +
                (respondTo?.let { "respondTo = $respondTo\n" } ?: "")
    }

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
                "consumeFrom is mandatory for RmqConsumableTestStep"
            )).size
            respondTo?.let {
                if (consumeChannelsSize != it.size)
                    throw TestStepCreationException("consumeFrom and respondTo must have the same size")
            }

            return RmqConsumableTestStep(
                id,
                name,
                timeout,
                values,
                testData,
                validationDetails,
                scenarioStateUpdaters,
                consumeFrom!!,
                respondTo,
                routingKey,
                properties
            )
        }
    }
}