package org.skellig.teststep.processor.rmq.model

import org.skellig.teststep.processing.exception.TestStepCreationException
import org.skellig.teststep.processing.model.ScenarioStateUpdater
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.ValidationNode

/**
 * Represents a test step that consumes data from RMQ queues and optionally responds to the other queues.
 * This test step is always executed asynchronously (ex. [execution] is [TestStepExecutionType.ASYNC]) as per
 * nature of a consumer behaviour.
 * Properties [attempts] and [delay] are not used.
 *
 * @property consumeFrom - List of queues to consume data from.
 * @property respondTo - Optional list of queues to respond to. If defined, the size of this list **must** be the same as [consumeFrom] and
 * position of a queue from [consumeFrom] must be the same as position of a queue from [respondTo] where you want to send the response.
 * For example:
 *```
 * consumeFrom [A, B, C]
 * respondTo [Ar, Br, Cr]
 *```
 * means that if a message consumed from queue `B`, the response will be sent to `Br` as it is in the same position in the list as `B`.
 *
 * @property routingKey - RMQ routing key for responses.
 * @property properties - RMQ queue properties.
 */
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
            val consumequeuesSize = (consumeFrom ?: throw TestStepCreationException(
                "consumeFrom is mandatory for RmqConsumableTestStep"
            )).size
            respondTo?.let {
                if (consumequeuesSize != it.size)
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