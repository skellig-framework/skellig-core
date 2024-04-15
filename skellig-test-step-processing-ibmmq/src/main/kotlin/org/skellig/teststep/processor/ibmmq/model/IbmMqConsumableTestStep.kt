package org.skellig.teststep.processor.ibmmq.model

import org.skellig.teststep.processing.exception.TestStepCreationException
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.ScenarioStateUpdater
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.ValidationNode

/**
 * Represents a test step that consumes data from IBMMQ queues and optionally responds to the other queues.
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
 */
open class IbmMqConsumableTestStep protected constructor(id: String?,
                                                         name: String?,
                                                         timeout: Int,
                                                         values: Map<String, Any?>?,
                                                         testData: Any?,
                                                         validationDetails: ValidationNode?,
                                                         scenarioStateUpdaters: List<ScenarioStateUpdater>?,
                                                         val consumeFrom: List<String>,
                                                         val respondTo: List<String>?)
    : DefaultTestStep(id, name!!, TestStepExecutionType.ASYNC, timeout, 0, 0, values,
                      testData, validationDetails, scenarioStateUpdaters) {


    override fun toString(): String {
        return super.toString() + "consumeFrom = $consumeFrom\n" +
                (respondTo?.let { "respondTo = $respondTo\n" } ?: "")
    }

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
            return IbmMqConsumableTestStep(id, name!!, timeout, values, testData,
                                           validationDetails, scenarioStateUpdaters, consumeFrom!!, respondTo)
        }
    }
}