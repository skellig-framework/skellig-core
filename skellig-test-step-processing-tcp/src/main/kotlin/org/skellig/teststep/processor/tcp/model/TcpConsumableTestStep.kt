package org.skellig.teststep.processor.tcp.model

import org.skellig.teststep.processing.exception.TestStepCreationException
import org.skellig.teststep.processing.model.ScenarioStateUpdater
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.ValidationNode

/**
 * Represents a test step that consumes data from TCP channels and optionally responds to the channels.
 * This test step is always executed asynchronously (ex. [execution] is [TestStepExecutionType.ASYNC]) as per
 * nature of a consumer behaviour.
 * Properties [attempts] and [delay] are not used.
 *
 * @property consumeFrom - List of channels to consume data from.
 * @property respondTo - Optional list of channels to respond to. If defined, the size of this list **must** be the same as [consumeFrom] and
 * position of a channel from [consumeFrom] must be the same as position of a channel from [respondTo] where you want to send the response.
 * For example:
 *```
 * consumeFrom [A, B, C]
 * respondTo [Ar, Br, Cr]
 *```
 * means that if a message consumed from channel `B`, the response will be sent to `Br` as it is in the same position in the list as `B`.
 *
 * @property readBufferSize - Read buffer size for the TCP connection.
 */
open class TcpConsumableTestStep protected constructor(
    id: String?,
    name: String?,
    timeout: Int,
    values: Map<String, Any?>?,
    testData: Any?,
    validationDetails: ValidationNode?,
    scenarioStateUpdaters: List<ScenarioStateUpdater>?,
    val consumeFrom: List<String>,
    val respondTo: List<String>?,
    readBufferSize: Int
) : BaseTcpTestStep(
    id, name!!, TestStepExecutionType.ASYNC, timeout, 0, 0, values,
    testData, validationDetails, scenarioStateUpdaters, readBufferSize
) {

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
                "consumeFrom is mandatory for TcpConsumableTestStep"
            )).size
            respondTo?.let {
                if (consumeChannelsSize != it.size)
                    throw TestStepCreationException("consumeFrom and respondTo must have the same size")
            }
            return TcpConsumableTestStep(
                id, name!!, timeout, values, testData,
                validationDetails, scenarioStateUpdaters, consumeFrom!!, respondTo, readBufferSize
            )
        }
    }
}